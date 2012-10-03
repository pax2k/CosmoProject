package no.pax.cosmo.Client;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import no.pax.cosmo.Util.Util;

import java.net.UnknownHostException;

/**
 * Created: rak
 * Date: 03.10.12
 */
public class MongoDBConnector {
    final String localhost = "localhost";
    final int port = 27017;
    final String dbName = "cosmo";
    final String todayDate = Util.getTodayDate();
    private DBCollection collection;

    public MongoDBConnector() {
        collection = connectToDataBase();
    }

    public Integer getBarkCounter() {
        Integer numberOfBarks;
        final boolean documentIsInDatabase = documentIsInDB();

        if (documentIsInDatabase) {
            final DBObject documentFromDatabase = getDocumentFromDatabase();
            numberOfBarks = (Integer) documentFromDatabase.get("barkCounter");
        } else {
            numberOfBarks = 0;
        }

        return numberOfBarks;
    }

    public Integer updateBarkCounter() {
        final Integer returnValue;
        final boolean documentIsInDatabase = documentIsInDB();
        System.out.println(documentIsInDatabase);

        if (documentIsInDatabase) {
            final DBObject documentFromDatabase = getDocumentFromDatabase();
            final Integer barkCounter = (Integer) documentFromDatabase.get("barkCounter") + 1;
            documentFromDatabase.put("barkCounter", barkCounter);
            collection.save(documentFromDatabase);
            returnValue = barkCounter;
            System.out.println("updated barklevel to: " + barkCounter);
        } else {
            createANewBarkCountDocument();
            returnValue = 1;
            System.out.println("Created a new document");
        }

        return returnValue;
    }

    private DBObject getDocumentFromDatabase() {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", todayDate);

        DBCursor cursor = collection.find(searchQuery);

        return cursor.next();
    }

    private boolean documentIsInDB() {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", todayDate);

        DBCursor cursor = collection.find(searchQuery);

        return cursor.size() > 0;
    }

    private void createANewBarkCountDocument() {
        BasicDBObject document = new BasicDBObject();
        document.put("date", todayDate);
        document.put("barkCounter", 1);

        collection.insert(document);
    }

    private DBCollection connectToDataBase() {
        try {
            Mongo m = new Mongo(localhost, port);
            DB db = m.getDB(dbName);

            return db.getCollection("barkCollection");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return null;
    }
}
