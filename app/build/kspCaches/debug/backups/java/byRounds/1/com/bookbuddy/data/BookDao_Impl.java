package com.bookbuddy.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BookDao_Impl implements BookDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Book> __insertionAdapterOfBook;

  private final DateConverters __dateConverters = new DateConverters();

  private final EntityDeletionOrUpdateAdapter<Book> __deletionAdapterOfBook;

  private final EntityDeletionOrUpdateAdapter<Book> __updateAdapterOfBook;

  private final SharedSQLiteStatement __preparedStmtOfUpdateBookStatus;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsCompleted;

  private final SharedSQLiteStatement __preparedStmtOfUpdateReadingStatus;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRanking;

  private final SharedSQLiteStatement __preparedStmtOfShiftRankings;

  public BookDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBook = new EntityInsertionAdapter<Book>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `books` (`id`,`name`,`author`,`author1`,`author2`,`author3`,`author4`,`author5`,`category`,`ranking`,`hasBook`,`status`,`startDate`,`endDate`,`createdAt`,`totalReadingDays`,`currentReadingStartDate`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Book entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getAuthor());
        if (entity.getAuthor1() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAuthor1());
        }
        if (entity.getAuthor2() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAuthor2());
        }
        if (entity.getAuthor3() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAuthor3());
        }
        if (entity.getAuthor4() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAuthor4());
        }
        if (entity.getAuthor5() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAuthor5());
        }
        statement.bindString(9, entity.getCategory());
        statement.bindLong(10, entity.getRanking());
        final int _tmp = entity.getHasBook() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final String _tmp_1 = __dateConverters.fromBookStatus(entity.getStatus());
        if (_tmp_1 == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, _tmp_1);
        }
        final Long _tmp_2 = __dateConverters.dateToTimestamp(entity.getStartDate());
        if (_tmp_2 == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, _tmp_2);
        }
        final Long _tmp_3 = __dateConverters.dateToTimestamp(entity.getEndDate());
        if (_tmp_3 == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, _tmp_3);
        }
        final Long _tmp_4 = __dateConverters.dateToTimestamp(entity.getCreatedAt());
        if (_tmp_4 == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, _tmp_4);
        }
        statement.bindLong(16, entity.getTotalReadingDays());
        final Long _tmp_5 = __dateConverters.dateToTimestamp(entity.getCurrentReadingStartDate());
        if (_tmp_5 == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, _tmp_5);
        }
      }
    };
    this.__deletionAdapterOfBook = new EntityDeletionOrUpdateAdapter<Book>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `books` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Book entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBook = new EntityDeletionOrUpdateAdapter<Book>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `books` SET `id` = ?,`name` = ?,`author` = ?,`author1` = ?,`author2` = ?,`author3` = ?,`author4` = ?,`author5` = ?,`category` = ?,`ranking` = ?,`hasBook` = ?,`status` = ?,`startDate` = ?,`endDate` = ?,`createdAt` = ?,`totalReadingDays` = ?,`currentReadingStartDate` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Book entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getAuthor());
        if (entity.getAuthor1() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getAuthor1());
        }
        if (entity.getAuthor2() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAuthor2());
        }
        if (entity.getAuthor3() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getAuthor3());
        }
        if (entity.getAuthor4() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAuthor4());
        }
        if (entity.getAuthor5() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAuthor5());
        }
        statement.bindString(9, entity.getCategory());
        statement.bindLong(10, entity.getRanking());
        final int _tmp = entity.getHasBook() ? 1 : 0;
        statement.bindLong(11, _tmp);
        final String _tmp_1 = __dateConverters.fromBookStatus(entity.getStatus());
        if (_tmp_1 == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, _tmp_1);
        }
        final Long _tmp_2 = __dateConverters.dateToTimestamp(entity.getStartDate());
        if (_tmp_2 == null) {
          statement.bindNull(13);
        } else {
          statement.bindLong(13, _tmp_2);
        }
        final Long _tmp_3 = __dateConverters.dateToTimestamp(entity.getEndDate());
        if (_tmp_3 == null) {
          statement.bindNull(14);
        } else {
          statement.bindLong(14, _tmp_3);
        }
        final Long _tmp_4 = __dateConverters.dateToTimestamp(entity.getCreatedAt());
        if (_tmp_4 == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, _tmp_4);
        }
        statement.bindLong(16, entity.getTotalReadingDays());
        final Long _tmp_5 = __dateConverters.dateToTimestamp(entity.getCurrentReadingStartDate());
        if (_tmp_5 == null) {
          statement.bindNull(17);
        } else {
          statement.bindLong(17, _tmp_5);
        }
        statement.bindLong(18, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateBookStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE books SET status = ?, startDate = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsCompleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE books SET status = ?, endDate = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateReadingStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE books SET status = ?, currentReadingStartDate = ?, totalReadingDays = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateRanking = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE books SET ranking = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfShiftRankings = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE books SET ranking = ranking + ? WHERE ranking >= ? AND ranking <= ? AND id != ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBook(final Book book, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBook.insertAndReturnId(book);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBook(final Book book, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBook.handle(book);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBook(final Book book, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBook.handle(book);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBookStatus(final long id, final BookStatus status, final Date startDate,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateBookStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __dateConverters.fromBookStatus(status);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 2;
        final Long _tmp_1 = __dateConverters.dateToTimestamp(startDate);
        if (_tmp_1 == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, _tmp_1);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateBookStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsCompleted(final long id, final BookStatus status, final Date endDate,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsCompleted.acquire();
        int _argIndex = 1;
        final String _tmp = __dateConverters.fromBookStatus(status);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 2;
        final Long _tmp_1 = __dateConverters.dateToTimestamp(endDate);
        if (_tmp_1 == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, _tmp_1);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkAsCompleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateReadingStatus(final long id, final BookStatus status,
      final Date currentReadingStartDate, final int totalReadingDays,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateReadingStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __dateConverters.fromBookStatus(status);
        if (_tmp == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, _tmp);
        }
        _argIndex = 2;
        final Long _tmp_1 = __dateConverters.dateToTimestamp(currentReadingStartDate);
        if (_tmp_1 == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindLong(_argIndex, _tmp_1);
        }
        _argIndex = 3;
        _stmt.bindLong(_argIndex, totalReadingDays);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateReadingStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRanking(final long id, final int ranking,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRanking.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, ranking);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateRanking.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object shiftRankings(final int fromRanking, final int toRanking, final int increment,
      final long excludeId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfShiftRankings.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, increment);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, fromRanking);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, toRanking);
        _argIndex = 4;
        _stmt.bindLong(_argIndex, excludeId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfShiftRankings.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Book>> getBooksToRead() {
    final String _sql = "SELECT * FROM books WHERE status != 'COMPLETED' ORDER BY ranking ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<Book>>() {
      @Override
      @NonNull
      public List<Book> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final List<Book> _result = new ArrayList<Book>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Book _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _item = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Book>> getCompletedBooks() {
    final String _sql = "SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY endDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<Book>>() {
      @Override
      @NonNull
      public List<Book> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final List<Book> _result = new ArrayList<Book>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Book _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _item = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Book>> getInProgressBooks() {
    final String _sql = "SELECT * FROM books WHERE status = 'IN_PROGRESS' ORDER BY startDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<Book>>() {
      @Override
      @NonNull
      public List<Book> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final List<Book> _result = new ArrayList<Book>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Book _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _item = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBooksToReadFiltered(final String author, final String category,
      final String titleSearch, final Continuation<? super List<Book>> $completion) {
    final String _sql = "\n"
            + "        SELECT * FROM books \n"
            + "        WHERE status != 'COMPLETED' \n"
            + "        AND (? IS NULL OR ? = '' OR \n"
            + "             author1 = ? OR author2 = ? OR author3 = ? OR author4 = ? OR author5 = ?)\n"
            + "        AND (? IS NULL OR ? = '' OR category = ?)\n"
            + "        AND (? IS NULL OR ? = '' OR LOWER(name) LIKE '%' || LOWER(?) || '%')\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 13);
    int _argIndex = 1;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 2;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 3;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 4;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 5;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 6;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 7;
    if (author == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, author);
    }
    _argIndex = 8;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    _argIndex = 9;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    _argIndex = 10;
    if (category == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, category);
    }
    _argIndex = 11;
    if (titleSearch == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, titleSearch);
    }
    _argIndex = 12;
    if (titleSearch == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, titleSearch);
    }
    _argIndex = 13;
    if (titleSearch == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, titleSearch);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Book>>() {
      @Override
      @NonNull
      public List<Book> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final List<Book> _result = new ArrayList<Book>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Book _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _item = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getBookById(final long id, final Continuation<? super Book> $completion) {
    final String _sql = "SELECT * FROM books WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Book>() {
      @Override
      @Nullable
      public Book call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final Book _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _result = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getTotalBooksRead() {
    final String _sql = "SELECT COUNT(*) FROM books WHERE status = 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getBooksReadThisYear(final long startOfYear,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM books WHERE status = 'COMPLETED' AND endDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startOfYear);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllCompletedBooks(final Continuation<? super List<Book>> $completion) {
    final String _sql = "SELECT * FROM books WHERE status = 'COMPLETED' ORDER BY endDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Book>>() {
      @Override
      @NonNull
      public List<Book> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final List<Book> _result = new ArrayList<Book>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Book _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _item = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllBooks(final Continuation<? super List<Book>> $completion) {
    final String _sql = "SELECT * FROM books ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Book>>() {
      @Override
      @NonNull
      public List<Book> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAuthor = CursorUtil.getColumnIndexOrThrow(_cursor, "author");
          final int _cursorIndexOfAuthor1 = CursorUtil.getColumnIndexOrThrow(_cursor, "author1");
          final int _cursorIndexOfAuthor2 = CursorUtil.getColumnIndexOrThrow(_cursor, "author2");
          final int _cursorIndexOfAuthor3 = CursorUtil.getColumnIndexOrThrow(_cursor, "author3");
          final int _cursorIndexOfAuthor4 = CursorUtil.getColumnIndexOrThrow(_cursor, "author4");
          final int _cursorIndexOfAuthor5 = CursorUtil.getColumnIndexOrThrow(_cursor, "author5");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfRanking = CursorUtil.getColumnIndexOrThrow(_cursor, "ranking");
          final int _cursorIndexOfHasBook = CursorUtil.getColumnIndexOrThrow(_cursor, "hasBook");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfTotalReadingDays = CursorUtil.getColumnIndexOrThrow(_cursor, "totalReadingDays");
          final int _cursorIndexOfCurrentReadingStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "currentReadingStartDate");
          final List<Book> _result = new ArrayList<Book>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Book _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAuthor;
            _tmpAuthor = _cursor.getString(_cursorIndexOfAuthor);
            final String _tmpAuthor1;
            if (_cursor.isNull(_cursorIndexOfAuthor1)) {
              _tmpAuthor1 = null;
            } else {
              _tmpAuthor1 = _cursor.getString(_cursorIndexOfAuthor1);
            }
            final String _tmpAuthor2;
            if (_cursor.isNull(_cursorIndexOfAuthor2)) {
              _tmpAuthor2 = null;
            } else {
              _tmpAuthor2 = _cursor.getString(_cursorIndexOfAuthor2);
            }
            final String _tmpAuthor3;
            if (_cursor.isNull(_cursorIndexOfAuthor3)) {
              _tmpAuthor3 = null;
            } else {
              _tmpAuthor3 = _cursor.getString(_cursorIndexOfAuthor3);
            }
            final String _tmpAuthor4;
            if (_cursor.isNull(_cursorIndexOfAuthor4)) {
              _tmpAuthor4 = null;
            } else {
              _tmpAuthor4 = _cursor.getString(_cursorIndexOfAuthor4);
            }
            final String _tmpAuthor5;
            if (_cursor.isNull(_cursorIndexOfAuthor5)) {
              _tmpAuthor5 = null;
            } else {
              _tmpAuthor5 = _cursor.getString(_cursorIndexOfAuthor5);
            }
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final int _tmpRanking;
            _tmpRanking = _cursor.getInt(_cursorIndexOfRanking);
            final boolean _tmpHasBook;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfHasBook);
            _tmpHasBook = _tmp != 0;
            final BookStatus _tmpStatus;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStatus);
            }
            final BookStatus _tmp_2 = __dateConverters.toBookStatus(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'com.bookbuddy.data.BookStatus', but it was NULL.");
            } else {
              _tmpStatus = _tmp_2;
            }
            final Date _tmpStartDate;
            final Long _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getLong(_cursorIndexOfStartDate);
            }
            _tmpStartDate = __dateConverters.fromTimestamp(_tmp_3);
            final Date _tmpEndDate;
            final Long _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getLong(_cursorIndexOfEndDate);
            }
            _tmpEndDate = __dateConverters.fromTimestamp(_tmp_4);
            final Date _tmpCreatedAt;
            final Long _tmp_5;
            if (_cursor.isNull(_cursorIndexOfCreatedAt)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getLong(_cursorIndexOfCreatedAt);
            }
            final Date _tmp_6 = __dateConverters.fromTimestamp(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.util.Date', but it was NULL.");
            } else {
              _tmpCreatedAt = _tmp_6;
            }
            final int _tmpTotalReadingDays;
            _tmpTotalReadingDays = _cursor.getInt(_cursorIndexOfTotalReadingDays);
            final Date _tmpCurrentReadingStartDate;
            final Long _tmp_7;
            if (_cursor.isNull(_cursorIndexOfCurrentReadingStartDate)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getLong(_cursorIndexOfCurrentReadingStartDate);
            }
            _tmpCurrentReadingStartDate = __dateConverters.fromTimestamp(_tmp_7);
            _item = new Book(_tmpId,_tmpName,_tmpAuthor,_tmpAuthor1,_tmpAuthor2,_tmpAuthor3,_tmpAuthor4,_tmpAuthor5,_tmpCategory,_tmpRanking,_tmpHasBook,_tmpStatus,_tmpStartDate,_tmpEndDate,_tmpCreatedAt,_tmpTotalReadingDays,_tmpCurrentReadingStartDate);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Integer> getBooksInQueueCount() {
    final String _sql = "SELECT COUNT(*) FROM books WHERE status != 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<String>> getAllAuthors() {
    final String _sql = "\n"
            + "        SELECT DISTINCT author FROM (\n"
            + "            SELECT author1 as author FROM books WHERE status != 'COMPLETED' AND author1 IS NOT NULL AND author1 != ''\n"
            + "            UNION\n"
            + "            SELECT author2 as author FROM books WHERE status != 'COMPLETED' AND author2 IS NOT NULL AND author2 != ''\n"
            + "            UNION\n"
            + "            SELECT author3 as author FROM books WHERE status != 'COMPLETED' AND author3 IS NOT NULL AND author3 != ''\n"
            + "            UNION\n"
            + "            SELECT author4 as author FROM books WHERE status != 'COMPLETED' AND author4 IS NOT NULL AND author4 != ''\n"
            + "            UNION\n"
            + "            SELECT author5 as author FROM books WHERE status != 'COMPLETED' AND author5 IS NOT NULL AND author5 != ''\n"
            + "        ) WHERE author IS NOT NULL AND author != ''\n"
            + "        ORDER BY author ASC\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<String>> getAllCategoriesForFilter() {
    final String _sql = "SELECT DISTINCT category FROM books WHERE status != 'COMPLETED'";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"books"}, new Callable<List<String>>() {
      @Override
      @NonNull
      public List<String> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final List<String> _result = new ArrayList<String>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final String _item;
            _item = _cursor.getString(0);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
