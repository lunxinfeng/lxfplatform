package com.fintech.lxf.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User user);

    @Delete
    void delAll(User... users);

    @Update
    void updateAll(User... users);

    @Query("SELECT * FROM User WHERE type = :type  ORDER BY `offset` DESC,pos_curr DESC LIMIT 1")
    User queryLast(int type);

    @Query("SELECT * FROM User WHERE type = :type")
    List<User> queryAll(int type);
}
