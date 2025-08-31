package nz.co.ethan.tsbbanking.service;


import nz.co.ethan.tsbbanking.controller.dto.customer.request.BindCustomerUserRequest;
import nz.co.ethan.tsbbanking.controller.dto.customer.request.CreateCustomerRequest;
import nz.co.ethan.tsbbanking.controller.dto.customer.response.CreateCustomerResponse;

public interface CustomerService {
    CreateCustomerResponse createCustomer (CreateCustomerRequest request);
    void bindCustomerWithUser(BindCustomerUserRequest req);
}
