package nz.co.ethan.tsbbanking.config.typehandler;

import nz.co.ethan.tsbbanking.domain.enums.UserTokenPurpose;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(UserTokenPurpose.class)
public class UserTokenPurposeHandler extends PgEnumTypeHandler<UserTokenPurpose> {
    public UserTokenPurposeHandler() {
        super(UserTokenPurpose.class, "user_token_purpose");
    }
}