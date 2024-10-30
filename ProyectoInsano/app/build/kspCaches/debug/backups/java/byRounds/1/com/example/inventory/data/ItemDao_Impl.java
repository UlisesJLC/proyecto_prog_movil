package com.example.inventory.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ItemDao_Impl implements ItemDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Item> __insertionAdapterOfItem;

  private final EntityDeletionOrUpdateAdapter<Item> __deletionAdapterOfItem;

  private final EntityDeletionOrUpdateAdapter<Item> __updateAdapterOfItem;

  public ItemDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfItem = new EntityInsertionAdapter<Item>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR IGNORE INTO `items` (`id`,`titulo`,`descripcion`,`clasificacion`,`horaCumplimiento`,`estado`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Item entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitulo());
        statement.bindString(3, entity.getDescripcion());
        statement.bindString(4, entity.getClasificacion());
        if (entity.getHoraCumplimiento() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getHoraCumplimiento());
        }
        final int _tmp = entity.getEstado() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfItem = new EntityDeletionOrUpdateAdapter<Item>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Item entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfItem = new EntityDeletionOrUpdateAdapter<Item>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `items` SET `id` = ?,`titulo` = ?,`descripcion` = ?,`clasificacion` = ?,`horaCumplimiento` = ?,`estado` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Item entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitulo());
        statement.bindString(3, entity.getDescripcion());
        statement.bindString(4, entity.getClasificacion());
        if (entity.getHoraCumplimiento() == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, entity.getHoraCumplimiento());
        }
        final int _tmp = entity.getEstado() ? 1 : 0;
        statement.bindLong(6, _tmp);
        statement.bindLong(7, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final Item item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfItem.insert(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final Item item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Item item, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfItem.handle(item);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Item>> getAllItems() {
    final String _sql = "SELECT * from items ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"items"}, new Callable<List<Item>>() {
      @Override
      @NonNull
      public List<Item> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitulo = CursorUtil.getColumnIndexOrThrow(_cursor, "titulo");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfClasificacion = CursorUtil.getColumnIndexOrThrow(_cursor, "clasificacion");
          final int _cursorIndexOfHoraCumplimiento = CursorUtil.getColumnIndexOrThrow(_cursor, "horaCumplimiento");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final List<Item> _result = new ArrayList<Item>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Item _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitulo;
            _tmpTitulo = _cursor.getString(_cursorIndexOfTitulo);
            final String _tmpDescripcion;
            _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            final String _tmpClasificacion;
            _tmpClasificacion = _cursor.getString(_cursorIndexOfClasificacion);
            final Long _tmpHoraCumplimiento;
            if (_cursor.isNull(_cursorIndexOfHoraCumplimiento)) {
              _tmpHoraCumplimiento = null;
            } else {
              _tmpHoraCumplimiento = _cursor.getLong(_cursorIndexOfHoraCumplimiento);
            }
            final boolean _tmpEstado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEstado);
            _tmpEstado = _tmp != 0;
            _item = new Item(_tmpId,_tmpTitulo,_tmpDescripcion,_tmpClasificacion,_tmpHoraCumplimiento,_tmpEstado);
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
  public Flow<Item> getItem(final int id) {
    final String _sql = "SELECT * from items WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"items"}, new Callable<Item>() {
      @Override
      @NonNull
      public Item call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitulo = CursorUtil.getColumnIndexOrThrow(_cursor, "titulo");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfClasificacion = CursorUtil.getColumnIndexOrThrow(_cursor, "clasificacion");
          final int _cursorIndexOfHoraCumplimiento = CursorUtil.getColumnIndexOrThrow(_cursor, "horaCumplimiento");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final Item _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitulo;
            _tmpTitulo = _cursor.getString(_cursorIndexOfTitulo);
            final String _tmpDescripcion;
            _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            final String _tmpClasificacion;
            _tmpClasificacion = _cursor.getString(_cursorIndexOfClasificacion);
            final Long _tmpHoraCumplimiento;
            if (_cursor.isNull(_cursorIndexOfHoraCumplimiento)) {
              _tmpHoraCumplimiento = null;
            } else {
              _tmpHoraCumplimiento = _cursor.getLong(_cursorIndexOfHoraCumplimiento);
            }
            final boolean _tmpEstado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEstado);
            _tmpEstado = _tmp != 0;
            _result = new Item(_tmpId,_tmpTitulo,_tmpDescripcion,_tmpClasificacion,_tmpHoraCumplimiento,_tmpEstado);
          } else {
            _result = null;
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
  public Flow<List<Item>> getItemClassi(final String clasificacion) {
    final String _sql = "SELECT * FROM items WHERE clasificacion = ? ORDER BY horaCumplimiento";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, clasificacion);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"items"}, new Callable<List<Item>>() {
      @Override
      @NonNull
      public List<Item> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitulo = CursorUtil.getColumnIndexOrThrow(_cursor, "titulo");
          final int _cursorIndexOfDescripcion = CursorUtil.getColumnIndexOrThrow(_cursor, "descripcion");
          final int _cursorIndexOfClasificacion = CursorUtil.getColumnIndexOrThrow(_cursor, "clasificacion");
          final int _cursorIndexOfHoraCumplimiento = CursorUtil.getColumnIndexOrThrow(_cursor, "horaCumplimiento");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final List<Item> _result = new ArrayList<Item>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Item _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpTitulo;
            _tmpTitulo = _cursor.getString(_cursorIndexOfTitulo);
            final String _tmpDescripcion;
            _tmpDescripcion = _cursor.getString(_cursorIndexOfDescripcion);
            final String _tmpClasificacion;
            _tmpClasificacion = _cursor.getString(_cursorIndexOfClasificacion);
            final Long _tmpHoraCumplimiento;
            if (_cursor.isNull(_cursorIndexOfHoraCumplimiento)) {
              _tmpHoraCumplimiento = null;
            } else {
              _tmpHoraCumplimiento = _cursor.getLong(_cursorIndexOfHoraCumplimiento);
            }
            final boolean _tmpEstado;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfEstado);
            _tmpEstado = _tmp != 0;
            _item = new Item(_tmpId,_tmpTitulo,_tmpDescripcion,_tmpClasificacion,_tmpHoraCumplimiento,_tmpEstado);
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
