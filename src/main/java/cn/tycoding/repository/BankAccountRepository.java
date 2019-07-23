package cn.tycoding.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import cn.tycoding.domain.BankAccount;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Integer> {

    BankAccount findBankAccountByIdAndType(int id,String type);

}
