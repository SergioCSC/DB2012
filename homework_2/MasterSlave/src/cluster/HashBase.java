package cluster;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created with IntelliJ IDEA.
 * User: Sergey Epifanov
 * Date: 9/12/12
 */


/** stores records in ArrayList.
 * each position in ArrayList also is a list.
 * This list consist of records which hashes is the same.
 */

public class HashBase implements Serializable {

    private final static int DEFAULT_BASE_SIZE = 1000;
    private final int baseSize;
    private final ArrayList<DBRecord> store;

    HashBase() {
        baseSize = DEFAULT_BASE_SIZE;
        store = new ArrayList<>(baseSize);
        initStore();
    }

    HashBase(int baseSize) {
        this.baseSize = baseSize;
        store = new ArrayList<>(baseSize);
        initStore();
        // TO DO: performance trouble

    }

    void initStore() {
        for (int i=0; i<baseSize; ++i) {
            store.add(new DBRecord("null_head_record",
                    new HashMap<String, String>()));
        }
    }

    void create(DBRecord record) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        DBRecord fatherRecord = find(record.id);
        if (fatherRecord != null) { // there is record with the same id in base
            System.out.println("Create: There is record with id == " + record.id +
                    " , do you want to update?");
            return;
        }

        int index = hash(record.id, baseSize);
        DBRecord endListRecord = store.get(index);
        while (endListRecord.next != null)  {
            endListRecord = endListRecord.next;
        }
        endListRecord.next = record;
    }

    DBRecord retrieve (String id) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        DBRecord result = find(id);
        if (result == null) {
            System.out.println("Retrieve: No record with id == " + id);
            return null;
        }
        return result.next;
    }

    void update(DBRecord record) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        DBRecord fatherRecord = find(record.id);
        if (fatherRecord == null) { // there is NO record with the same id
            create(record);
        } else {
            record.next = fatherRecord.next.next;
            fatherRecord.next = record;
        }
    }

    void delete(String id) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        DBRecord fatherRecord = find(id);
        if (fatherRecord == null) { // nothing to delete
            System.out.println("Delete: There is no record with id == " + id);
            return;
        }
        fatherRecord.next = fatherRecord.next.next;
    }

    static int hash(String id, int valueRange) throws
            NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] recordInBytes = id.getBytes("UTF-8");
        byte[] hashInBytes = md.digest(recordInBytes);

        // convert hashInBytes to int
        int result = 0;
        for (int i : hashInBytes) {
            result *= 256;
            result += i + 128;
            result %= valueRange;
        }
        return  result;
    }

    /**
     *
     *
     * @param id unique id of record
     * @return returns _father_ of element with given id
     * if store has it, otherwise - null
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */

    private DBRecord find(String id) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        int index = hash(id, baseSize);
        DBRecord record = store.get(index);
        while ((record.next != null) && (! record.next.id.equals(id)))  {
            record = record.next;
        }
        return (record.next != null ? record : null);
    }

    public void print(){
          for (DBRecord dbRecord:store){
              System.out.println(dbRecord.toString());
          }
    }
}
