package com.ldw.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.media.audiofx.Visualizer;
import android.view.View;

public class VisualizerView extends View implements Visualizer.OnDataCaptureListener {

    private final static char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final int DN_W = 470;
    private static final int DN_H = 360;
    private static final int DN_SL = 15;
    private static final int DN_SW = 5;

    private int hgap = 0;
    private int vgap = 0;
    private int levelStep = 0;
    private float strokeWidth = 0;
    private float strokeLength = 0;

    protected final static int MAX_LEVEL = 30;

    protected final static int CYLINDER_NUM = 9;

    protected Visualizer mVisualizer = null;

    protected Paint mPaint = null;

    protected byte[] mData = new byte[CYLINDER_NUM];

    protected int[] temp = new int[CYLINDER_NUM];

    boolean mDataEn = true;
    private OnFftListener mListener;
    private float gain = 1;


    public VisualizerView(Context context) {
        super(context);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);

        mPaint.setStrokeJoin(Join.ROUND);
        mPaint.setStrokeCap(Cap.ROUND);
    }

    //do Layout
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        float w, h, xr, yr;

        w = right - left;
        h = bottom - top;
        xr = w / (float) DN_W;
        yr = h / (float) DN_H;

        strokeWidth = DN_SW * yr;
        strokeLength = DN_SL * xr;
        hgap = (int) ((w - strokeLength * CYLINDER_NUM) / (CYLINDER_NUM + 1));
        vgap = (int) (h / (MAX_LEVEL + 2));
        //vgap = (int) ((h - strokeWidth * MAX_LEVEL) / (MAX_LEVEL + 1));

        mPaint.setStrokeWidth(strokeWidth);
    }


    protected void drawCylinder(Canvas canvas, float x, byte value) {
        if (value <= 0) {
            value = 1;
        }
        for (int i = 0; i < value; i++) {
            float y = (getHeight() / 2 - i * vgap - vgap);
            float y1 = (getHeight() / 2 + i * vgap + vgap);

            mPaint.setColor(Color.WHITE);
            canvas.drawLine(x, y, (x + strokeLength), y, mPaint);


            if (i <= 6 && value > 0) {
                mPaint.setColor(Color.WHITE);
                mPaint.setAlpha(100 - (100 / 6 * i));
                canvas.drawLine(x, y1, (x + strokeLength), y1, mPaint);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        //int j=-1;
        for (int i = CYLINDER_NUM - 1; i >= 0; i--) {

            drawCylinder(canvas, strokeWidth / 2 + hgap + i * (hgap + strokeLength), mData[i]);
        }

        //for(int i = CYLINDER_NUM-1; i>=0; i--){
        //j++;
        //drawCylinder(canvas, strokeWidth / 2 + hgap + j * (hgap + strokeLength), mData[i]);
        //}
    }

    /**
     * It sets the visualizer of the view. DO set the viaulizer to null when exit the program.
     *
     * @parma visualizer It is the visualizer to set.
     */
    public void setVisualizer(Visualizer visualizer) {
        if (visualizer != null) {
            if (!visualizer.getEnabled()) {
                visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            }
            levelStep = 240 / MAX_LEVEL;
            visualizer.setDataCaptureListener(this, Visualizer.getMaxCaptureRate() / 2, false, true);

        } else {

            if (mVisualizer != null) {
                mVisualizer.setEnabled(false);
                mVisualizer.release();
            }
        }
        mVisualizer = visualizer;
    }


    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {


        byte[] model = new byte[fft.length / 2 + 1];
        if (mDataEn) {
            model[0] = (byte) Math.abs(fft[1]);
            int j = 1;
            for (int i = 2; i < fft.length; ) {
                //for (int i = 2; i < 21; ) {
                model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
                i += 2;
                j++;
            }
        } else {
            for (int i = 0; i < CYLINDER_NUM; i++) {
                model[i] = 0;
            }
        }
        for (int i = 0; i < CYLINDER_NUM; i++) {
            for (int j = 0; j < 9; j++) {
                temp[i] += (int) (Math.abs(model[j + i * 9 + 1]) / levelStep);
            }
            temp[i] = temp[i] / 2;
        }

        temp[0] -= 0;
        temp[1] -= 0;
        temp[2] -= 0;
        temp[3] -= 0;
        temp[4] -= 0;
        temp[5] -= 0;
        temp[6] -= 0;
        temp[7] -= 0;
        temp[8] -= 0;

        StringBuffer sbAscii = new StringBuffer();
        sbAscii.append("@");
        StringBuffer sbHex = new StringBuffer();
        sbHex.append(byte2hex((byte) 0x55));
        sbHex.append(byte2hex((byte) 0xAA));
        sbHex.append(byte2hex((byte) 0x01));
        sbHex.append(byte2hex((byte) 0x11));

        for (int i = 0; i < CYLINDER_NUM; i++) {
            final byte a = (byte) (temp[i]);

            //final byte b = mData[i];

            temp[i] = 0;

            //if (a > b) {
            mData[i] = (byte) (a * gain);
            //} else {
            //if (b > 0) {
            //mData[i]--;
            //}
            //}
            sbAscii.append(formatNum(mData[i] & 0xFF));
            sbHex.append(byte2hex(mData[i]));

        }

        sbAscii.append("#");
        sbHex.append(byte2hex((byte) 0xFB));
        sbHex.append(byte2hex((byte) 0xDB));

        if (mListener != null) {
            mListener.onFft(sbAscii.toString(), sbHex.toString());
        }

        postInvalidate();
    }


    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        // Do nothing...
    }

    public interface OnFftListener {
        void onFft(String dataAscii, String dataHex);
    }

    private String formatNum(int num) {
        if (num < 10) {
            return "00" + num;
        } else if (num < 100) {
            return "0" + num;
        } else {
            return String.valueOf(num);
        }
    }

    public OnFftListener getListener() {
        return mListener;
    }

    public void setListener(OnFftListener listener) {
        mListener = listener;
    }

    public float getGain() {
        return gain;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    private String byte2hex(byte b) {

        StringBuffer buf = new StringBuffer();
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
        return buf.toString();
    }
}
