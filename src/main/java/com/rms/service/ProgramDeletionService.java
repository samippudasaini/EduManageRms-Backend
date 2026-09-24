package com.rms.service;

import com.rms.repository.ProgramRepository;
import com.rms.repository.ResultRepository;
import com.rms.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgramDeletionService {

    @Autowired private ProgramRepository repo;
    @Autowired private ResultRepository resultRepo;
    @Autowired private StudentRepository studentRepo;

    public static class BlockedByResultsException extends RuntimeException {
        public final long resultCount;
        public BlockedByResultsException(long resultCount) {
            super("Program has recorded results");
            this.resultCount = resultCount;
        }
    }

    public static class BlockedByStudentsException extends RuntimeException {
        public final long studentCount;
        public BlockedByStudentsException(long studentCount) {
            super("Program has enrolled students");
            this.studentCount = studentCount;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAndFlush(Long id, boolean force) {
        long resultCount = resultRepo.countByProgramId(id);
        if (resultCount > 0) {
            if (!force) {
                throw new BlockedByResultsException(resultCount);
            }
            resultRepo.deleteResultMarksByProgramId(id);
            resultRepo.deleteResultsByProgramId(id);
        }

        long studentCount = studentRepo.findByProgramId(id).size();
        if (studentCount > 0) {
            if (!force) {
                throw new BlockedByStudentsException(studentCount);
            }
            studentRepo.unenrollFromProgram(id);
        }

        repo.detachFromExaminations(id);
        repo.deleteById(id);
        repo.flush();
    }
}