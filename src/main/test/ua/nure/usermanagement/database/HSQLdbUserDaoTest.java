package ua.nure.usermanagement.database;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.junit.Before;
import ua.nure.usermanagement.User;
import ua.nure.usermanagement.database.exception.DatabaseException;

import java.util.*;

public class HSQLdbUserDaoTest extends DatabaseTestCase {

    private HSQLdbUserDao dao;
    private ConnectionFactory connectionFactory;

    @Override
    protected IDatabaseConnection getConnection() throws Exception {
        connectionFactory = new ConnectionFactoryImpl("org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:db/usermanager", "sa", "");
        return new DatabaseConnection(connectionFactory.createConnection());
    }

    @Override
    protected IDataSet getDataSet() throws Exception {
        return new XmlDataSet(getClass().getClassLoader().getResourceAsStream("usersDataSet.xml"));
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        dao = new HSQLdbUserDao(connectionFactory);
    }


    public void testCreate() {
        try {
            User user = new User();
            user.setFirstName("Jerry");
            user.setLastName("Smith");
            user.setDateOfBirth(new Date());
            assertNull(user.getId());
            User createdUser = dao.create(user);
            assertNotNull(createdUser);
            assertNotNull(createdUser.getId());
            assertEquals(user.getFullName(), createdUser.getFullName());
        } catch (DatabaseException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testUpdate() throws Exception {
        try {
            User morty = new User();
            morty.setId(1001L);
            morty.setFirstName("Morty");
            morty.setLastName("Sanchez");
            Calendar calendar = Calendar.getInstance();
            calendar.set(1999, Calendar.JULY, 25, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            morty.setDateOfBirth(calendar.getTime());
            dao.update(morty);
            User updatedUser = dao.find(1001L);

            assertEquals(morty, updatedUser);
        } catch (DatabaseException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testDelete() throws Exception {
        try {
            User morty = new User();
            morty.setId(1001L);
            morty.setFirstName("Morty");
            morty.setLastName("Sanchez");
            Calendar calendar = Calendar.getInstance();
            calendar.set(1999, Calendar.JULY, 25, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            morty.setDateOfBirth(calendar.getTime());
            dao.delete(morty);
            User deletedUser = dao.find(1001L);
            assertNotSame("Entry wasn't deleted",morty, deletedUser);
        } catch (DatabaseException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testFind() throws Exception {
        User morty = new User();
        morty.setId(1001L);
        morty.setFirstName("Morty");
        morty.setLastName("Smith");
        Calendar calendar = Calendar.getInstance();
        calendar.set(1999, Calendar.JULY, 25, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        morty.setDateOfBirth(calendar.getTime());

        User result = dao.find(1001L);

        assertEquals("Wrong first name",morty.getFirstName(),result.getFirstName());
        assertEquals("Wrong last name",morty.getLastName(),result.getLastName());
        assertEquals("Wrong id",morty.getId(),result.getId());
        assertEquals("Wrong date of birth",morty.getDateOfBirth(),result.getDateOfBirth());
    }

    public void testFindAll() throws Exception {
        assertNotNull(dao.findAll());
        int AMOUNT_OF_ENTRIES_IN_DB = 2;
        assertEquals("Collection size: ", AMOUNT_OF_ENTRIES_IN_DB, dao.findAll().size());
    }

}