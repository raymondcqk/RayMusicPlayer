package com.raymondqk.raymusicplayer.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.raymondqk.raymusicplayer.R;

/**
 * Created by 陈其康 raymondchan on 2016/8/3 0003.
 */
public class AvatarCircle extends ImageView {

    //view的宽度
    private int mWidth;
    //圆形的半径
    private int mRadius;
    //位图着色器
    private BitmapShader mBitmapShader;
    //矩阵 -- 用于缩放图片以适应view控件的大小
    private Matrix mMatrix;
    //圆形图像的paint
    private Paint mBitmapPaint;
    //用于绘制圆形图片的外边界
    private Paint mBorderPaint;
    //边界宽度
    private float mStkroeWidth;
    //边界颜色
    private int mStrokeColor;


    //三种构造器
    public AvatarCircle(Context context) {
        super(context);
        inti(context, null);
    }

    public AvatarCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        inti(context, attrs);
    }

    public AvatarCircle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inti(context, attrs);
    }

    //测量大小
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 因为我们要绘制圆形，所以强制使宽高一致，以小的值为准，以防在布局使用控件时设置height、width不一致的情况
         */
        mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
        mRadius = mWidth / 2;
        setMeasuredDimension(mWidth, mWidth);
        //因为我们需要绘制外边界以及阴影，所以绘制的图形的尺寸（半径）需要缩小，否则边界就会超出view的区域
        //我们这里为了保证图片以及阴影的完整，图形内缩两倍边界的宽度
        mRadius = (int) (mRadius - 2*mStkroeWidth);
    }


    /**
     * 设置BitmapShader及涂料Paint
     */
    private void setBitmapShader() {
        //首先获得drawable对象，也就是控件属性的src，也就是我们的图片喇
        Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        //将图片转换为Bitmap
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        //use the bitmap to create a BitmapShader 将bitmap载入着色器，后面两个参数为x，y轴的缩放模式，CLAMP代表拉伸
        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        // initial a scale in float 初始化图片与view之间的伸缩比例，因为比例一般非整数，用float
        float scale = 1.0f;
        //get the bitmap's min one between its height and width 将图片的宽高的小者作为图片的边长，用来和view计算伸缩比例
        int bitmapSize = Math.min(bitmap.getWidth(), bitmap.getHeight());
        //calculate the scale between bitmap and view 计算缩放比例 view的大小与图片大小之间的比例
        scale = mWidth * 1.0f / bitmapSize;  // warn: use float to avoid some problem cause by precision
        //set the 变换矩阵 with scale  设置变换矩阵的伸缩比例，长宽均以相同比例伸缩
        mMatrix.setScale(scale, scale);//bitmapShader's width and height both set the scale
        //给shader设置变换矩阵，绘制时就会根据view的size，设置图片的size
        // 使图片的小边缩放到与view大小一致，这样就避免了图片过小导致CLAMP或过大导致显示不完全
        mBitmapShader.setLocalMatrix(mMatrix);
        //use the shader to set the Paint 给paint上shader涂料（自己的表达，不官方）
        mBitmapPaint.setShader(mBitmapShader);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //        super.onDraw(canvas); 这里记得去掉父类的onDraw，否则会先绘制ImageView默认的图案

        if (getDrawable() == null) {
            return;
        }

        //绘制内部圆形图片
        setBitmapShader();
        canvas.drawCircle(mWidth / 2, mWidth / 2, mRadius, mBitmapPaint);
        //绘制外边界
        setBorderPaint();
        canvas.drawCircle(mWidth / 2, mWidth / 2, mRadius, mBorderPaint);
    }

//    /**
//     * 将图片转换为Bitmap的工具函数
//     *
//     * @param drawable
//     * @return
//     */
//    private Bitmap drawableToBitmap(Drawable drawable) {
//        BitmapDrawable bd = (BitmapDrawable) drawable;
//        return bd.getBitmap();
//    }

    /**
     * 初始化
     *
     * @param context
     * @param attrs
     */
    private void inti(Context context, AttributeSet attrs) {
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBorderPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);//反锯齿
        mBorderPaint.setAntiAlias(true);
        //获取布局文件里面自定义控件设置的属性值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AvatarCircle);
        mStkroeWidth = typedArray.getDimension(R.styleable.AvatarCircle_StrokeWidth, 0);
        mStrokeColor = typedArray.getColor(R.styleable.AvatarCircle_StrokeColor, Color.WHITE);
        typedArray.recycle();//回收属性对象


    }

    /**
     * 设置边界笔刷piant
     */
    private void setBorderPaint() {
        //set border paint
        mBorderPaint.setStyle(Paint.Style.STROKE);//设置笔刷样式：原区域掏空，只画边界
        mBorderPaint.setColor(mStrokeColor);//给笔刷上色
        mBorderPaint.setStrokeCap(Paint.Cap.ROUND);//边界类型为圆形
        mBorderPaint.setStrokeWidth(mStkroeWidth);//设置边界宽度
        //设置阴影
        this.setLayerType(LAYER_TYPE_SOFTWARE, mBorderPaint);
        mBorderPaint.setShadowLayer(12.0f, 3.0f, 3.0f, Color.BLACK);
        //end 设置阴影
    }
}
