package nz.co.ethan.tsbbanking.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import nz.co.ethan.tsbbanking.common.BizException;
import nz.co.ethan.tsbbanking.common.ErrorCodes;
import nz.co.ethan.tsbbanking.controller.dto.customer.request.BindCustomerUserRequest;
import nz.co.ethan.tsbbanking.controller.dto.customer.request.CreateCustomerRequest;
import nz.co.ethan.tsbbanking.controller.dto.customer.response.CreateCustomerResponse;
import nz.co.ethan.tsbbanking.domain.customer.Customer;
import nz.co.ethan.tsbbanking.domain.customer.CustomerUser;
import nz.co.ethan.tsbbanking.domain.enums.CustomerUserAccessRole;
import nz.co.ethan.tsbbanking.domain.user.User;
import nz.co.ethan.tsbbanking.mapper.customer.CustomerMapper;
import nz.co.ethan.tsbbanking.mapper.customer.CustomerUserMapper;
import nz.co.ethan.tsbbanking.mapper.userAuth.UserMapper;
import nz.co.ethan.tsbbanking.service.CustomerService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerMapper customerMapper;
    private final CustomerUserMapper customerUserMapper;
    private final UserMapper userMapper;

    @Override
    public CreateCustomerResponse createCustomer(CreateCustomerRequest req) {

        Customer c = new Customer();
        c.setFirstName(req.getFirstName().trim());
        c.setLastName(req.getLastName().trim());
        c.setPreferredName(Strings.emptyToNull(req.getPreferredName()));
        c.setEmail(Strings.emptyToNull(req.getEmail()));
        c.setContactNumber(Strings.emptyToNull(req.getContactNumber()));
        c.setAddressLine1(req.getAddressLine1().trim());
        c.setAddressLine2(Strings.emptyToNull(req.getAddressLine2()));
        c.setSuburb(Strings.emptyToNull(req.getSuburb()));
        c.setCity(req.getCity().trim());
        c.setRegion(req.getRegion().trim());
        c.setPostcode(req.getPostcode().trim());
        c.setCountry(Strings.isNullOrEmpty(req.getCountry()) ? "New Zealand" : req.getCountry().trim());

        customerMapper.insert(c);


        String displayName = Optional.ofNullable(c.getPreferredName())
                .filter(s -> !s.isBlank())
                .orElse(c.getFirstName() + " " + c.getLastName());

        return new CreateCustomerResponse(c.getId(), displayName, c.getCreatedAt());
    }

    @Transactional
    @Override
    public void bindCustomerWithUser(BindCustomerUserRequest req) {

        Long customerId = req.getCustomerId();
        // 1) Check customer
        Customer c = customerMapper.selectById(customerId);
        if (c == null) throw new BizException(ErrorCodes.CUSTOMER_NOT_FOUND.code(), "Customer not found: " + customerId);

        // 2) Check user
        String uname = req.getUsername().trim().toLowerCase(Locale.ROOT);
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, uname));
        if (u == null) throw new BizException(ErrorCodes.USER_NOT_FOUND.code(), "User not found: " + uname);

        CustomerUser existing = customerUserMapper.selectOne(new LambdaQueryWrapper<CustomerUser>()
                .eq(CustomerUser::getCustomerId, customerId)
                .eq(CustomerUser::getUserId, u.getId()));
        if (existing != null) {
            throw new BizException(ErrorCodes.BIND_EXISTS.code(), "Customer already bind with this user: " + customerId);
        }

        // 5) Bind
        CustomerUser cu = new CustomerUser();
        cu.setCustomerId(customerId);
        cu.setUserId(u.getId());
        cu.setAccessRole(req.getRole());

        try {
            customerUserMapper.insert(cu);
        } catch (DataIntegrityViolationException e) {
            // In concurrent scenarios, UNIQUE(customer_id, user_id) may be violated;
            // perform a lookup again to return an idempotent result.
            CustomerUser again = customerUserMapper.selectOne(new LambdaQueryWrapper<CustomerUser>()
                    .eq(CustomerUser::getCustomerId, customerId)
                    .eq(CustomerUser::getUserId, u.getId()));
            if (again == null) throw e;
        }

    }

}
