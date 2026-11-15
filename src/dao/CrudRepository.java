package dao;

import java.util.List;

/**
 * Interface Generic CrudRepository
 * Menerapkan prinsip Abstraksi pada lapisan Data Access Object (DAO)
 * untuk standarisasi operasi Create, Read, Update, Delete.
 *
 * @param <T> Tipe entitas model
 */
public interface CrudRepository<T> {
    void tambah(T data) throws Exception;
    void ubah(T data) throws Exception;
    void hapus(String id) throws Exception;
    List<T> ambilSemua() throws Exception;
    T cariBerdasarkanId(String id) throws Exception;
}
