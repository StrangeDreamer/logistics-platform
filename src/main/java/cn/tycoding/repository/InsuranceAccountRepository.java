package cn.tycoding.repository;
import cn.tycoding.domain.InsuranceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *  @Author: qlXie
 *  @Date: 2019-07-02 11:10
 */
@Repository
public interface InsuranceAccountRepository extends JpaRepository<InsuranceAccount,Integer> {

    InsuranceAccount findInsuranceAccountByIdAndType(int id, String type);
}



