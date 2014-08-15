package org.example.mqttclient;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import org.example.mqttclient.R;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.tools.ZoomEvent;
import org.achartengine.tools.ZoomListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ChartFragment extends Fragment implements OnClickListener {

	private static Random RAND = new Random();
	private static final String TIME = "H:mm:ss";
	//private static final String[] ITEMS = { "A", "B", "C", "D", "E", "F" };
	private static final String[] ITEMS = { "AD[A3] value"};
	private final int[] COLORS = { randomColor(), randomColor(), randomColor(), randomColor(), randomColor(), randomColor() };

	private static final int[] THRESHOLD_VALUES = { 500, 800, 1100 };
	private static final int[] THRESHOLD_COLORS = { Color.RED, Color.YELLOW, Color.GREEN };
	private static final String[] THRESHOLD_LABELS = { "Bad", "Good", "Excellent" };

	private static final int TEN_SEC = 10000;
	private static final int TWO_SEC = 2000;
	private static final float RATIO = 0.618033988749895f;

	private View mViewZoomIn;
	private View mViewZoomOut;
	private View mViewZoomReset;
	private GraphicalView mChartView;
	private XYSeriesRenderer[] mThresholdRenderers;
	private XYMultipleSeriesRenderer mRenderer;
	private XYMultipleSeriesDataset mDataset;
	private HashMap<String, TimeSeries> mSeries;
	private TimeSeries[] mThresholds;
	private ArrayList<String> mItems;
	private double mYAxisMin = Double.MAX_VALUE;    
	private double mYAxisMax = Double.MIN_VALUE;
	private double mZoomLevel = 1;
	//private double mLastItemChange;
	//������ı�����ΪLong
	private long mLastItemChange;
	private int mItemIndex;
	private int mYAxisPadding = 5;

	//����BroadcastReceiver������Service����������Ϣ
	private BroadcastReceiver messageReceiver;
    private void InitMessageListener(){
    	messageReceiver = new BroadcastReceiver(){
    		@Override
    		public void onReceive(Context context, Intent intent){
    	        //Mqtt Service���յ��ƶ���������,����һ���㲥��Ϣ,MainActivity�յ�֮���������
    			 double jn516x_ad = intent.getDoubleExtra("adc", 0.0);  
    			 Log.v("DATA", "AD:"+jn516x_ad);    			     			 
    			 addValue(jn516x_ad);
    		}
    	};
    	 
    	//ע�ᴫ���������¼�
    	this.getActivity().registerReceiver(messageReceiver, new IntentFilter("sensor_event"));
    }
	
    //�ȴ�15Min ÿ�����2S
//	private final CountDownTimer mTimer = new CountDownTimer(15 * 60 * 1000, 500) {
//		@Override
//		public void onTick(final long millisUntilFinished) {
//			addValue(randomValue());
//		}
//
//		@Override
//		public void onFinish() {}
//	};

	private final ZoomListener mZoomListener = new ZoomListener() {
		@Override
		public void zoomReset() {
			mZoomLevel = 1;
			scrollGraph(new Date().getTime());
		}

		@Override
		public void zoomApplied(final ZoomEvent event) {
			if (event.isZoomIn()) {
				mZoomLevel /= 2;
			}
			else {
				mZoomLevel *= 2;
			}
			scrollGraph(new Date().getTime());
		}
	};

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mItems = new ArrayList<String>();
		mSeries = new HashMap<String, TimeSeries>();    //һ�����Ƹ�ʱ�����е��HashMap
		mDataset = new XYMultipleSeriesDataset();       //���������ݼ�
		mRenderer = new XYMultipleSeriesRenderer();     //��������Ⱦ��

		mRenderer.setLabelsColor(Color.LTGRAY);
		mRenderer.setAxesColor(Color.LTGRAY);
		mRenderer.setGridColor(Color.rgb(136, 136, 136));  //����դ����ɫ
		mRenderer.setBackgroundColor(Color.BLACK);         //���ñ�����ɫ
		mRenderer.setApplyBackgroundColor(true);           //Ӧ�ñ�����ɫ

		mRenderer.setLegendTextSize(20);                  //���ñ�����ֳߴ�
		mRenderer.setLabelsTextSize(20);                  //�����������ֳߴ�
		mRenderer.setPointSize(8);                        //����������С
		mRenderer.setMargins(new int[] { 60, 60, 60, 60 });

		mRenderer.setFitLegend(true);
		mRenderer.setShowGrid(true);
		mRenderer.setZoomEnabled(true);
		mRenderer.setExternalZoomEnabled(true);
		mRenderer.setAntialiasing(true);
		mRenderer.setInScroll(true);

		mLastItemChange = new Date().getTime();
		mItemIndex = Math.abs(RAND.nextInt(ITEMS.length));
		InitMessageListener();		
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
			mYAxisPadding = 9;
			mRenderer.setYLabels(15);
		}

		final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.chart, container, false);
		mChartView = ChartFactory.getTimeChartView(getActivity(), mDataset, mRenderer, TIME);
		mChartView.addZoomListener(mZoomListener, true, false);
		view.addView(mChartView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		/*mViewZoomIn = getActivity().findViewById(R.id.zoom_in);
		mViewZoomOut = getActivity().findViewById(R.id.zoom_out);
		mViewZoomReset = getActivity().findViewById(R.id.zoom_reset);
		mViewZoomIn.setOnClickListener(this);
		mViewZoomOut.setOnClickListener(this);
		mViewZoomReset.setOnClickListener(this);*/

		mThresholds = new TimeSeries[3];
		mThresholdRenderers = new XYSeriesRenderer[3];

		for (int i = 0; i < THRESHOLD_COLORS.length; i++) {
			mThresholdRenderers[i] = new XYSeriesRenderer();
			mThresholdRenderers[i].setColor(THRESHOLD_COLORS[i]);
			mThresholdRenderers[i].setLineWidth(3);

			mThresholds[i] = new TimeSeries(THRESHOLD_LABELS[i]);
			final long now = new Date().getTime();
			mThresholds[i].add(new Date(now - 1000 * 60 * 10), THRESHOLD_VALUES[i]);
			mThresholds[i].add(new Date(now + 1000 * 60 * 10), THRESHOLD_VALUES[i]);

			mDataset.addSeries(mThresholds[i]);
			mRenderer.addSeriesRenderer(mThresholdRenderers[i]);
		}

		//mTimer.start();
		//InitMessageListener();
	}

	@Override
	public void onStop() {
		super.onStop();
		/*if (null != mTimer) {
			mTimer.cancel();
		}*/
		//getActivity().unregisterReceiver(messageReceiver);
	}


	private double randomValue() {
		final int value = Math.abs(RAND.nextInt(32));
		final double percent = (value * 100) / 31.0;
		return ((int) (percent * 10)) / 10.0;
	}
 
	//������������Լ��ػ�
	private void addValue(double value) {
		//final double value = randomValue();
		//�������������ݵķ�Χ	
		if (mYAxisMin > value) mYAxisMin = value;
		if (mYAxisMax < value) mYAxisMax = value;

		final Date now = new Date();
		final long time = now.getTime();        //return millisecond value

		if (time - mLastItemChange > 10000) {   //�������ݸ���ʱ�����10s
			mLastItemChange = time;
			mItemIndex = Math.abs(RAND.nextInt(ITEMS.length));   //ITEMS["A", "B"] length=6 abs����ֵ
		}

		final String item = ITEMS[mItemIndex];   //ȡItem������ A����B...
		final int color = COLORS[mItemIndex];    //�������ȡ��
		final int lastItemIndex = mItems.lastIndexOf(item); //Searches this list for the specified object and returns the index of the last occurrence
		mItems.add(item);

		//�ҵ���Item��
		if (lastItemIndex > -1) {
			boolean otherItemBetween = false;
			for (int i = lastItemIndex + 1; i < mItems.size(); i++) {
				if (!item.equals(mItems.get(i))) {
					otherItemBetween = true;
					break;
				}
			}
			if (otherItemBetween) {
				addSeries(null, now, value, item, color);     //���һ��������
			}
			else {
				mSeries.get(item).add(now, value);            //��ԭ�����������һ����
			}
		}
		else {
			addSeries(item, now, value, item, color);
		}

		scrollGraph(time);
		mChartView.repaint();
	}

	private void addSeries(final String title, final Date time, final double value, final String item, final int color) {
		for (int i = 0; i < THRESHOLD_COLORS.length; i++) {
			mThresholds[i].add(new Date(time.getTime() + 1000 * 60 * 5), THRESHOLD_VALUES[i]);
		}

		final TimeSeries series = new TimeSeries(title);
		series.add(time, value);
		mSeries.put(item, series);
		mDataset.addSeries(series);
		mRenderer.addSeriesRenderer(getSeriesRenderer(color));
	}

	private void scrollGraph(final long time) {
		final double[] limits = new double[] { time - TEN_SEC * mZoomLevel, time + TWO_SEC * mZoomLevel, mYAxisMin - mYAxisPadding,
				mYAxisMax + mYAxisPadding };
		mRenderer.setRange(limits);
	}

	private XYSeriesRenderer getSeriesRenderer(final int color) {
		final XYSeriesRenderer r = new XYSeriesRenderer();
		r.setDisplayChartValues(true);
		r.setChartValuesTextSize(30);
		r.setPointStyle(PointStyle.CIRCLE);
		r.setColor(color);
		r.setFillPoints(true);
		r.setLineWidth(4);
		return r;
	}

	private static int randomColor() {
		final float hue = (RAND.nextInt(360) + RATIO);
		return Color.HSVToColor(new float[] { hue, 0.8f, 0.9f });
	}

	@Override
	public void onClick(final View v) {
		/*switch (v.getId()) {
		case R.id.zoom_in:
			mChartView.zoomIn();
			break;

		case R.id.zoom_out:
			mChartView.zoomOut();
			break;

		case R.id.zoom_reset:
			mChartView.zoomReset();
			break;

		default:
			break;
		}*/

	}
}
