package gds.music;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.AvoidXfermode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

/**
 * Author:  gds
 * Time: 2016/5/25 14:07
 * E-mail: guodongshenggds@foxmail.com
 */
public class MyProgress extends ProgressBar {

    private static final int  DEFAULTE_REACH_COLOR = 0xffc00d1d;
    private static final int  DEFAULTE__REACH_HEIGHT = 2;

    private static final int  DEFAULTE_UNREACH_COLOR = 0xffd3d6da;
    private static final int  DEFAULTE__UNREACH_HEIGHT = 2;

    private static final int  DEFAULTE_TEXT_COLOR = DEFAULTE_REACH_COLOR  ;
    private static final int  DEFAULTE_TEXT_SIZE = 10;
    private static final int  DEFAULTE_TEXT_OFFSET = 10;

    private int reach_color = DEFAULTE_REACH_COLOR;
    private int reach_height = dpTopx(DEFAULTE__REACH_HEIGHT);

    private int unreach_color = DEFAULTE_UNREACH_COLOR;
    private int unreach_height = dpTopx(DEFAULTE__UNREACH_HEIGHT);

    private int text_color = DEFAULTE_TEXT_COLOR;
    private int text_size = spTopx(DEFAULTE_TEXT_SIZE);
    private int text_offset = dpTopx(DEFAULTE_TEXT_OFFSET);

    Paint paint =new Paint();

    private int realWidth;

    public MyProgress(Context context) {
        this(context,null);
    }

    public MyProgress(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //获取自定义属性
        getMyAttrs(attrs);
    }

    public void getMyAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs,R.styleable.MyProgress);

        reach_color = ta.getColor(R.styleable.MyProgress_reach_color,reach_color);
        reach_height = (int) ta.getDimension(R.styleable.MyProgress_reach_height,reach_height);

        unreach_color = ta.getColor(R.styleable.MyProgress_unreach_color,unreach_color);
        unreach_height = (int) ta.getDimension(R.styleable.MyProgress_unreach_height,unreach_height);

        text_color = ta.getColor(R.styleable.MyProgress_text_color,text_color);
        text_size = (int) ta.getDimension(R.styleable.MyProgress_text_size,text_size);
        text_offset = (int) ta.getDimension(R.styleable.MyProgress_text_offset,text_offset);

        ta.recycle();
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthVal = MeasureSpec.getSize(widthMeasureSpec);
        int heightVal = measureHeight(heightMeasureSpec);
        setMeasuredDimension(widthVal,heightVal);
        realWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int measureHeight(int heightMeasureSpec) {
        int result =0;
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if(mode == MeasureSpec.EXACTLY){
            result = size;
        }
        else{
            int textHeight = (int) (paint.descent() - paint.ascent());
            result = getPaddingTop() + getPaddingBottom()+Math.max(
                    Math.max(reach_height,unreach_height),Math.abs(textHeight));
            if(mode == MeasureSpec.AT_MOST){
                result = Math.min(result,size);
            }
            return result;
        }

        return 0;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(),getHeight()/2);
        boolean noNeedUnreach = false;

        String text = getProgress()+"%";
        int textWidth =(int)paint.measureText(text);
        float radio = getProgress()*1.0f/getMax();
        float progressX = radio * realWidth;
        if(progressX+textWidth>realWidth){
            progressX = realWidth;
            noNeedUnreach =true;
        }
        float endX = radio * realWidth - text_offset / 2;
        if(endX > 0){
            paint.setColor(reach_color);
            paint.setStrokeWidth(realWidth);
            canvas.drawLine(0,0,endX,0,paint);
        }

        paint.setColor(text_color);
        int y = (int) (-(paint.descent() + paint.ascent())/2);
        canvas.drawText(text,progressX,y,paint);

        if(!noNeedUnreach){
            float start = progressX + text_offset/2 + textWidth;
            paint.setColor(unreach_color);
            paint.setStrokeWidth(unreach_height);
            canvas.drawLine(start,0,realWidth,0,paint);
        }

        canvas.restore();

    }

    public int dpTopx(int val){
       return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,val,getResources().getDisplayMetrics());
    }

    public int spTopx(int val){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,val,getResources().getDisplayMetrics());
    }


}
