package com.taskflow.seed;

import com.taskflow.entity.Board;
import com.taskflow.entity.BoardMember;
import com.taskflow.entity.BoardRole;
import com.taskflow.entity.Card;
import com.taskflow.entity.TaskList;
import com.taskflow.entity.User;
import com.taskflow.repository.BoardMemberRepository;
import com.taskflow.repository.BoardRepository;
import com.taskflow.repository.CardRepository;
import com.taskflow.repository.TaskListRepository;
import com.taskflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Seeds an instantly explorable demo board on the {@code dev} profile so the app
 * is not empty on first run. Idempotent: skips if the demo user already exists.
 */
@Component
@Profile("dev")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String DEMO_EMAIL = "demo@taskflow.dev";

    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final BoardMemberRepository memberRepository;
    private final TaskListRepository listRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository,
                          BoardRepository boardRepository,
                          BoardMemberRepository memberRepository,
                          TaskListRepository listRepository,
                          CardRepository cardRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.memberRepository = memberRepository;
        this.listRepository = listRepository;
        this.cardRepository = cardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.existsByEmail(DEMO_EMAIL)) {
            return;
        }
        log.info("Seeding demo data (dev profile)");

        User demo = userRepository.save(User.builder()
                .email(DEMO_EMAIL)
                .displayName("Demo User")
                .passwordHash(passwordEncoder.encode("password123"))
                .build());

        User teammate = userRepository.save(User.builder()
                .email("alex@taskflow.dev")
                .displayName("Alex Rivera")
                .passwordHash(passwordEncoder.encode("password123"))
                .build());

        Board board = boardRepository.save(Board.builder()
                .name("Product Roadmap")
                .description("Sample board showcasing real-time collaboration")
                .owner(demo)
                .build());

        memberRepository.save(BoardMember.builder()
                .board(board)
                .user(teammate)
                .role(BoardRole.MEMBER)
                .build());

        TaskList backlog = createList(board, "Backlog", 1024);
        TaskList inProgress = createList(board, "In Progress", 2048);
        TaskList done = createList(board, "Done", 3072);

        createCard(backlog, "Design board layout", "Trello-style columns with smooth drag", 1024,
                teammate, "design", null);
        createCard(backlog, "Set up CI pipeline", "GitHub Actions: build + test backend and frontend",
                2048, null, "infra", null);
        createCard(inProgress, "Implement drag-and-drop", "dnd-kit with fractional positions", 1024,
                demo, "feature", Instant.now().plus(3, ChronoUnit.DAYS));
        createCard(inProgress, "Wire up STOMP sync", "Broadcast card moves over /topic/boards/{id}",
                2048, demo, "feature,realtime", null);
        createCard(done, "Project scaffolding", "Monorepo, Spring Boot + Vite React", 1024,
                demo, "chore", null);

        log.info("Demo login: {} / password123", DEMO_EMAIL);
    }

    private TaskList createList(Board board, String name, double position) {
        return listRepository.save(TaskList.builder()
                .board(board)
                .name(name)
                .position(position)
                .build());
    }

    private void createCard(TaskList list, String title, String description, double position,
                            User assignee, String labels, Instant dueDate) {
        cardRepository.save(Card.builder()
                .list(list)
                .title(title)
                .description(description)
                .position(position)
                .assignee(assignee)
                .labels(labels)
                .dueDate(dueDate)
                .build());
    }
}
