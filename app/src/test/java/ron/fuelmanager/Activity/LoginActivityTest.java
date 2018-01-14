package ron.fuelmanager.Activity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import android.content.SharedPreferences;


import static org.junit.Assert.*;

/**
 * Created by amit on 1/14/2018.
 */


@RunWith(MockitoJUnitRunner.class)
public class LoginActivityTest {

    @Test
    public void isEmailValid() throws Exception {
        LoginActivity loginActivity = new LoginActivity();
        assertEquals(loginActivity.isEmailValid("amit"),false);
        assertEquals(loginActivity.isEmailValid("amit@outlook.co.il"),true);
    }

    @Test
    public void isPasswordValid() throws Exception {
        LoginActivity loginActivity = new LoginActivity();
        assertEquals(loginActivity.isPasswordValid("123456789"),true);
        assertEquals(loginActivity.isPasswordValid("12345678"),true);
        assertEquals(loginActivity.isPasswordValid("1234567"),true);
        assertEquals(loginActivity.isPasswordValid("123456"),true);
        assertEquals(loginActivity.isPasswordValid("12345"),false);
        assertEquals(loginActivity.isPasswordValid("1234"),false);
        assertEquals(loginActivity.isPasswordValid("123"),false);
        assertEquals(loginActivity.isPasswordValid("12"),false);
        assertEquals(loginActivity.isPasswordValid("1"),false);
        assertEquals(loginActivity.isPasswordValid(""),false);
    }


}