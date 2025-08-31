package nz.co.ethan.tsbbanking.mapper.account;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import nz.co.ethan.tsbbanking.domain.account.Transfer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferMapper extends BaseMapper<Transfer> {}
