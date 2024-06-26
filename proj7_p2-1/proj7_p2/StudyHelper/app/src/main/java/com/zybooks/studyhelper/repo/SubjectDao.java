package com.zybooks.studyhelper.repo;

import androidx.room.*;
import com.zybooks.studyhelper.model.Subject;
import java.util.List;
import androidx.lifecycle.LiveData;

@Dao
public interface SubjectDao {
    @Query("SELECT * FROM Subject WHERE id = :id")
    LiveData<Subject> getSubject(long id);

    @Query("SELECT * FROM Subject ORDER BY text COLLATE NOCASE")
    LiveData<List<Subject>> getSubjects();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long addSubject(Subject subject);

    @Update
    void updateSubject(Subject subject);

    @Delete
    void deleteSubject(Subject subject);
}
