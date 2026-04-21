package com.mollubook.domain.comment.repository;

import com.mollubook.domain.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

	List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);

	long countByPostId(Long postId);

	long countByParentId(Long parentId);
}
