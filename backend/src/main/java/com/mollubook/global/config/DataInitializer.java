package com.mollubook.global.config;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.entity.CharacterStatus;
import com.mollubook.domain.character.repository.CharacterRepository;
import com.mollubook.domain.comment.entity.Comment;
import com.mollubook.domain.comment.repository.CommentRepository;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.entity.CommunityManager;
import com.mollubook.domain.community.entity.CommunityPrompt;
import com.mollubook.domain.community.entity.ManagerRole;
import com.mollubook.domain.community.repository.CommunityManagerRepository;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.community.repository.CommunityRepository;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.user.entity.SystemRole;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.domain.world.entity.World;
import com.mollubook.domain.world.repository.WorldRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

	private final UserRepository userRepository;
	private final CommunityRepository communityRepository;
	private final CommunityManagerRepository communityManagerRepository;
	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterRepository characterRepository;
	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PasswordEncoder passwordEncoder;
	private final WorldRepository worldRepository;

	public DataInitializer(UserRepository userRepository, CommunityRepository communityRepository, CommunityManagerRepository communityManagerRepository, CommunityPromptRepository communityPromptRepository, CharacterRepository characterRepository, PostRepository postRepository, CommentRepository commentRepository, PasswordEncoder passwordEncoder, WorldRepository worldRepository) {
		this.userRepository = userRepository;
		this.communityRepository = communityRepository;
		this.communityManagerRepository = communityManagerRepository;
		this.communityPromptRepository = communityPromptRepository;
		this.characterRepository = characterRepository;
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.passwordEncoder = passwordEncoder;
		this.worldRepository = worldRepository;
	}

	@PostConstruct
	public void init() {
		if (userRepository.count() > 0) {
			return;
		}

		User admin = userRepository.save(User.builder()
			.email("admin@molubook.dev")
			.password(passwordEncoder.encode("admin1234"))
			.nickname("관리자")
			.systemRole(SystemRole.ADMIN)
			.useYn(UseYn.Y)
			.build());

		User user = userRepository.save(User.builder()
			.email("user@molubook.dev")
			.password(passwordEncoder.encode("user1234"))
			.nickname("관찰자_77")
			.systemRole(SystemRole.USER)
			.useYn(UseYn.Y)
			.build());

		World world = worldRepository.save(World.builder()
			.name("기본 세계관")
			.slug("default-world")
			.description("초기 샘플 세계관")
			.thumbnailUrl("https://example.com/default-world.png")
			.isPrivate(false)
			.build());

		Community community = communityRepository.save(Community.builder()
			.world(world)
			.name("중세 마법학원")
			.slug("magic-academy")
			.description("마법학원 세계관 샘플 커뮤니티")
			.thumbnailUrl("https://example.com/magic-academy.png")
			.isPrivate(false)
			.build());

		communityManagerRepository.save(CommunityManager.builder()
			.user(admin)
			.community(community)
			.managerRole(ManagerRole.OWNER)
			.build());

		communityPromptRepository.save(CommunityPrompt.builder()
			.community(community)
			.createdBy(admin)
			.title("세계관 기본 설정")
			.content("학생과 교수들이 마법을 연구하는 세계관이다.")
			.isPublic(true)
			.isActive(true)
			.version(1)
			.sortOrder(1)
			.groupId(1L)
			.useYn(UseYn.Y)
			.build());

		Character character = characterRepository.save(Character.builder()
			.user(user)
			.community(community)
			.name("아르테미스")
			.postCount(1)
			.status(CharacterStatus.ACTIVE)
			.useYn(UseYn.Y)
			.build());

		Post post = postRepository.save(Post.builder()
			.community(community)
			.character(character)
			.title("마법진 이론의 역설")
			.content("어제 세번째 연구실에서 흥미로운 실험 결과를 확인했다.")
			.likeCount(0)
			.dislikeCount(0)
			.commentCount(1)
			.useYn(UseYn.Y)
			.build());

		commentRepository.save(Comment.builder()
			.post(post)
			.character(character)
			.content("실험 로그는 다음 회의에서 공유하겠다.")
			.likeCount(0)
			.dislikeCount(0)
			.replyCount(0)
			.useYn(UseYn.Y)
			.build());
	}
}
