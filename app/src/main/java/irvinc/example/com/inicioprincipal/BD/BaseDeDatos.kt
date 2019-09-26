package irvinc.example.com.inicioprincipal.BD

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BaseDeDatos(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table Usuarios(usuario text, correo text, contra text)")

        db.execSQL("create table Recicladoras(usuario text, correo text, contra text, nombre text, telefono text, calle text, colonia text, numeroInt text)")
        db.execSQL("create table Materiales(usuario text, material text, precio double, unidad text)")
        db.execSQL("create table Ubicacion(usuario text, latitud double, longitud double)")

        db.execSQL("create table Calificacion(usuarioRecicladora text, calificacion float, usuarioCliente text)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }
}