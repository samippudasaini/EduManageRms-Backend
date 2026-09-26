package com.rms.service;

import com.rms.repository.MarksRepository;
import com.rms.repository.ProgramSubjectRepository;
import com.rms.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubjectDeletionService {
    @Autowired
    private SubjectRepository repo;
    @Autowired private MarksRepository marksRepo;
    @Autowired private ProgramSubjectRepository programSubjectRepo;

    public static class BlockedByMarksException extends RuntimeException {
        public final long marksCount;
        public BlockedByMarksException(long marksCount) {
            super("Subject has recorded marks");
            this.marksCount = marksCount;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAndFlush(Long id, boolean force) {
        long marksCount = marksRepo.countBySubjectId(id);
        if (marksCount > 0) {
            if (!force) {
                throw new BlockedByMarksException(marksCount);
            }
            marksRepo.deleteBySubjectId(id); // permanently deletes those marks
        }

        programSubjectRepo.deleteBySubjectId(id);
        repo.deleteById(id);
        repo.flush();
    }
}
