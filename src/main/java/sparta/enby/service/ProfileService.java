package sparta.enby.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sparta.enby.dto.AttendedBoardDto;
import sparta.enby.dto.MyBoardResponseDto;
import sparta.enby.dto.RegisteredBoardDto;
import sparta.enby.model.Board;
import sparta.enby.model.Registration;
import sparta.enby.repository.AccountRepository;
import sparta.enby.repository.BoardRepository;
import sparta.enby.repository.RegistrationRepository;
import sparta.enby.repository.ReviewRepository;
import sparta.enby.security.UserDetailsImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final BoardRepository boardRepository;
    private final AccountRepository accountRepository;
    private final ReviewRepository reviewRepository;
    private final RegistrationRepository registrationRepository;

    public ResponseEntity<Object> getProfile(String name, UserDetailsImpl userDetails) {
        List<Registration> registrations = registrationRepository.findAllByCreatedBy(name);
        List<Registration> acceptedList = registrationRepository.findAllByAcceptedTrue();
        List<Object> toList = new ArrayList<>();

        //신청한 모임
        List<RegisteredBoardDto> registeredBoardList = new ArrayList<>();
        //참가한 모임
        List<AttendedBoardDto> attendedBoardList = new ArrayList<>();
        // 생성한 모임
        List<MyBoardResponseDto> myboardList = new ArrayList<>();

        //신청한 모임 Mapping
        for (Registration registration : registrations) {
            List<Board> boardList = boardRepository.findAllByRegistrations(registration);
            registeredBoardList.addAll(boardList.stream().map(
                    board -> new RegisteredBoardDto(
                            board.getId(),
                            board.getTitle(),
                            board.getBoard_imgUrl(),
                            board.getLocation(),
                            board.getMeetTime(),
                            board.getPeople_current(),
                            board.getPeople_max(),
                            board.getAccount().getNickname(),
                            board.getAccount().getProfile_img()
                    )
            ).collect(Collectors.toList()));
        }
        for (Registration registration : acceptedList) {
            List<Board> attendedBoard = boardRepository.findAllByRegistrations(registration);
            attendedBoardList.addAll(attendedBoard.stream().map(
                    board -> new AttendedBoardDto(
                            board.getId(),
                            board.getTitle(),
                            board.getBoard_imgUrl(),
                            board.getLocation(),
                            board.getMeetTime(),
                            board.getPeople_current(),
                            board.getPeople_max()
                    )
            ).collect(Collectors.toList()));
        }
        List<Board> myboards = boardRepository.findAllByCreatedBy(name);
        myboardList = myboards.stream().map(
                board -> new MyBoardResponseDto(
                        board.getId(),
                        board.getTitle(),
                        board.getMeetTime(),
                        board.getCreatedAt()
                )
        ).collect(Collectors.toList());
        toList = Stream.concat(registeredBoardList.stream(), attendedBoardList.stream()).collect(Collectors.toList());
        toList = Stream.concat(toList.stream(), myboardList.stream()).collect(Collectors.toList());
        return ResponseEntity.ok().body(toList);
    }
}