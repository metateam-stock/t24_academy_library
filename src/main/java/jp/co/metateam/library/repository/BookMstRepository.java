package jp.co.metateam.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.Stock;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface BookMstRepository extends JpaRepository<BookMst, Long> {
	List<BookMst> findAll();

	Optional<BookMst> findById(BigInteger id);

	// SQLでデータを取得「書籍名」
	@Query("SELECT title FROM BookMst")
	List<String> findAllTitles();

	// SQLでデータを取得「全ての書籍ID」
	@Query("SELECT id FROM BookMst")
	int[] findAllId();
}
