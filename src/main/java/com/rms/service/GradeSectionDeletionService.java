package com.rms.service;

import com.rms.repository.AssignmentRepository;
import com.rms.repository.AssignmentStudentRepository;
import com.rms.repository.GradeSectionMappingRepository;
import com.rms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

    @Service
    public class GradeSectionDeletionService {

//        @Autowired
//        private GradeSectionMappingRepository repo;
//        @Autowired private StudentRepository studentRepo;
//        @Autowired private AssignmentRepository assignmentRepo;
//
//        public static class BlockedByStudentsException extends RuntimeException {
//            public final long studentCount;
//            public BlockedByStudentsException(long studentCount) {
//                super("Grade-section has enrolled students");
//                this.studentCount = studentCount;
//            }
//        }
//
//        @Transactional(propagation = Propagation.REQUIRES_NEW)
//        public void deleteAndFlush(Long id, boolean force) {
//            long studentCount = studentRepo.findByGradeSectionId(id).size();
//            if (studentCount > 0) {
//                if (!force) {
//                    throw new BlockedByStudentsException(studentCount);
//                }
//                studentRepo.unenrollFromGradeSection(id); // unenroll, don't delete the students
//            }
//
//            assignmentRepo.deleteByGradeSectionId(id); // existing cascade behavior, kept as-is
//            repo.deleteById(id);
//            repo.flush(); // force the DB constraint check now, inside this transaction
//        }

        @Autowired private GradeSectionMappingRepository repo;
        @Autowired private StudentRepository studentRepo;
        @Autowired private AssignmentRepository assignmentRepo;
        @Autowired private AssignmentStudentRepository assignmentStudentRepo;

        public static class BlockedByStudentsException extends RuntimeException {
            public final long studentCount;
            public BlockedByStudentsException(long studentCount) {
                super("Grade-section has enrolled students");
                this.studentCount = studentCount;
            }
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void deleteAndFlush(Long id, boolean force) {
            long studentCount = studentRepo.findByGradeSectionId(id).size();
            if (studentCount > 0) {
                if (!force) {
                    throw new BlockedByStudentsException(studentCount);
                }
                studentRepo.unenrollFromGradeSection(id);
            }

            // AssignmentStudent rows reference assignment_id — must go first,
            // or deleting Assignment rows below hits the same FK violation.
            assignmentStudentRepo.deleteByGradeSectionId(id);
            assignmentRepo.deleteByGradeSectionId(id);

            repo.deleteById(id);
            repo.flush();
        }
    }
