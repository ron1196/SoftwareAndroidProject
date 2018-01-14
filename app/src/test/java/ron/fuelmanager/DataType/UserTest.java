package ron.fuelmanager.DataType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by amit on 1/14/2018.
 */


@RunWith(MockitoJUnitRunner.class)
public class UserTest {
    @Mock
    Car mMockCar;

    @Test
    public void toStringTest() throws Exception {
        when(mMockCar.toString()).thenReturn("MOCKED CAR");
        User user = new User("test","me","testvile","fake",mMockCar);
        assertEquals(user.toString() , "User{firstName='test', lastName='me', city='testvile', address='fake', budget=0.0, car=MOCKED CAR}");
    }

}