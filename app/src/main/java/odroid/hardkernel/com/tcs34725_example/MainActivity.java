package odroid.hardkernel.com.tcs34725_example;

import androidx.appcompat.app.AppCompatActivity;
import nz.geek.android.things.drivers.colour.Colour;
import nz.geek.android.things.drivers.colour.ColourSensor;
import odroid.hardkernel.com.tcs34725_example.Lcd.LCD;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ColourSensor.Listener{
    String TAG = "tcs34725";

    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createLcd();
        createColourSensor();

        btn = findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colourSensor.enable(false);
            }
        });
    }

    public float map(float value, float low1, float high1, float low2, float high2) {
        return low2 + (high2 - low2) * ((value - low1) / (high1 - low1));
    }

    @Override
    public void onColourUpdated(int clear, int red, int green, int blue) {
        try {
            // Need to calibrate before use the values.
            lcd.print(String.format(Locale.UK, "cct: %6d K", Colour.toColourTemperature(red, green, blue)), 1);
            lcd.print(String.format(Locale.UK, "Lux: %6d", Colour.toLux(red, green, blue)), 2);
            lcd.print(String.format(Locale.UK, "R %5d G %5d", red, green), 3);
            lcd.print(String.format(Locale.UK, "B %5d", blue), 4);

            {
                int convClear = (int) map(clear, 16, 3072, 0, 255);
                int convRed = (int) map(red, 16, 3072, 0, 255);
                int convGreen = (int) map(green, 10, 3072, 0, 255);
                int convBlue = (int) map(blue, 4, 2298, 0, 255);

                Log.d(TAG, "( " + convRed + ", " + convGreen + ", " + convBlue + " )");
                btn.setBackgroundColor(Color.argb(convClear, convRed, convGreen, convBlue));
            }
            //btn.setBackgroundColor(Colour.toColourTemperature(red, green, blue));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (colourSensor != null) {
            colourSensor.enable(false);
            colourSensor.close();
        }
    }

    private final int LCD_WIDTH = 20;
    private final int LCD_HEIGHT = 4;
    private LCD lcd;

    private ColourSensor colourSensor;
    private final String LED_GPIO_NAME = "22";

    private void createLcd() {
        try {
            lcd = new LCD(LCD_WIDTH, LCD_HEIGHT);
            lcd.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createColourSensor() {
        ColourSensor.ColourSensorBuilder builder = ColourSensor.builder();
        colourSensor = builder.withLedGpio(LED_GPIO_NAME).withListener(this).build();
        colourSensor.enable(true);
    }
}
