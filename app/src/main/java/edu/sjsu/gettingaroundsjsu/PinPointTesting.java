package edu.sjsu.gettingaroundsjsu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PinPointTesting extends AppCompatActivity {

    private RelativeLayout rl_Main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Hello","Ehllo");
        setContentView(R.layout.activity_pin_point_testing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        rl_Main = (RelativeLayout) findViewById(R.id.relativelayout);
        View v = new MyView(getApplicationContext(),0,0);
        v.setLayoutParams(rl_Main.getLayoutParams());

        rl_Main.addView(v);



    }

    class MyView extends View{


        Paint paint = new Paint();
        Point point = new Point();
        public MyView(Context context, int x, int y) {
            super(context);
            paint.setColor(Color.RED);
            paint.setStrokeWidth(15);
            paint.setStyle(Paint.Style.FILL);
            point.x=x;
            point.y=y;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Bitmap b= BitmapFactory.decodeResource(getResources(), R.drawable.transparent);
            canvas.drawBitmap(b, 0, 0, paint);
            canvas.drawCircle(point.x, point.y, 50, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    point.x = event.getX();
                    point.y = event.getY();

            }
            invalidate();
            return true;

        }

    }
    class Point {
        float x, y;
    }

}
