package com.couchcoding.oola.service;

import com.couchcoding.oola.dto.study.response.StudyCreationDto;
import com.couchcoding.oola.dto.study.response.StudyProgressDto;
import com.couchcoding.oola.dto.studymember.response.StudyMemberResponseDto;
import com.couchcoding.oola.entity.Member;
import com.couchcoding.oola.entity.Study;
import com.couchcoding.oola.entity.StudyMember;
import com.couchcoding.oola.repository.StudyMemberRepository;
import com.couchcoding.oola.repository.StudyRepository;
import com.couchcoding.oola.repository.StudyMemberRepositoryCustom;
import com.couchcoding.oola.validation.StudyNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StudyMemberService {
    private final StudyMemberRepositoryCustom studyMemberRepositoryCustom;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;

    // 스터디 참여자 정보조회
    @Transactional
    public List<StudyMember> studyMembers(Long studyId) {
        List<StudyMember> studyMembers = studyMemberRepositoryCustom.findAllByStudyId(studyId);
        return studyMembers;
    }

    // 스터디 개설시 리더정보 추가
    public StudyMember setStudyLeader(StudyMember studyMember) {
        return studyMemberRepository.saveAndFlush(studyMember);
    }

    // 스터디 참여 신청 (멤버)
    @Transactional
    public StudyMemberResponseDto studyMemberEnroll(Member member, Long studyId) {
        Study study = studyRepository.findById(studyId).orElseThrow(() -> {
            throw new StudyNotFoundException();
        });

        StudyMember studyMember = StudyMember.builder()
                .study(study)
                .role("member")
                .member(member)
                .build();

        log.info("member techStack: {}" + member.getTechStack().toString());

        StudyMember entityResult = studyMemberRepository.saveAndFlush(studyMember);
        log.info("entityResult: {}" + entityResult);

        int updateParticipants = study.getCurrentParticipants() + 1;
        Study entity = study.updateCurrentParticipants( updateParticipants);
        StudyMemberResponseDto studyMemberResponseDto = new StudyMemberResponseDto();
        studyMemberResponseDto.setMember(entityResult.getMember());
        studyMemberResponseDto.setStudy(entity);
        return studyMemberResponseDto;
    }

    // 마이스터디 - 내가 개설한 스터디 조회
    public List<StudyCreationDto> mystudies(Member member) {
        List<StudyCreationDto> studyCreationDtoList = new ArrayList<>();
       StudyCreationDto studyCreationDto = null;
        String role = "leader";
        // role과 member의 uid 사용하여 검색
        Long uid = member.getId();
        List<StudyMember> studyMembers = studyMemberRepositoryCustom.findAllByUidAndRole(uid, role);
        for (StudyMember studyMember : studyMembers) {
            Study study = studyMember.getStudy();
            studyCreationDto = new StudyCreationDto(study);
            studyCreationDtoList.add(studyCreationDto);
        }
        return studyCreationDtoList;
    }

    // 마이스터디 - 내가 참여한 스터디 조회
    public List<StudyProgressDto> myJoinStudies(Member member) {
        List<StudyProgressDto> studyProgressDtos = new ArrayList<>();
        StudyProgressDto studyProgressDto = null;
        String role = "member";
        Long uid = member.getId();
        String status = "진행";
        List<StudyMember> studyMembers = studyMemberRepositoryCustom.findAllByUidAndRoleAndStatus(uid, role, status);
        for (StudyMember studyMember : studyMembers) {
            Study study = studyMember.getStudy();
            studyProgressDto = new StudyProgressDto(study);
            studyProgressDtos.add(studyProgressDto);
        }
        return studyProgressDtos;
    }
}