package nz.co.ethan.tsbbanking.mapper.userAuth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nz.co.ethan.tsbbanking.domain.user.UserToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserTokenMapper extends BaseMapper<UserToken> {
}
