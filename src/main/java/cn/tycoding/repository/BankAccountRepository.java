package cn.tycoding.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import cn.tycoding.domain.BankAccount;

/**
 * @Author: qlXie
 * @Date: 2019-07-02 11:10
 */
public interface BankAccountRepository extends JpaRepository<BankAccount,Integer> {

    BankAccount findBankAccountByIdAndType(int id,String type);

}
