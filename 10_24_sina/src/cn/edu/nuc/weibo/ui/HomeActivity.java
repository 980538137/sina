package cn.edu.nuc.weibo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.edu.nuc.weibo.R;
import cn.edu.nuc.weibo.adapter.WeiboAdapter;
import cn.edu.nuc.weibo.bean.Status;
import cn.edu.nuc.weibo.bean.Task;
import cn.edu.nuc.weibo.logic.IWeiboActivity;
import cn.edu.nuc.weibo.logic.MainService;
import cn.edu.nuc.weibo.widget.PullToRefreshListView;
import cn.edu.nuc.weibo.widget.PullToRefreshListView.OnRefreshListener;
import cn.edu.nuc.weibo.widget.PullToRefreshListView.onLoadOldListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class HomeActivity extends BaseActivity implements IWeiboActivity {
	private PullToRefreshListView lv_weibo = null;
	private Button btn_say = null;
	private Button btn_refresh = null;
	private LinearLayout ll_loading = null;
	private long max_id = 0;
	private WeiboAdapter adapter = null;
	private Animation rotateAnimation = null;

	public static final int INITIATE = 1;
	public static final int MORE_NEW = 2;
	public static final int MORE_OLD = 3;
	
	private int mCurrentState = INITIATE;
	private List<Status> mStatuses = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		init();
	}

	@Override
	public void init() {
		// 刷新旋转动画
		rotateAnimation = AnimationUtils.loadAnimation(HomeActivity.this,
				R.anim.refresh_animation);
		// 正在加载布局
		ll_loading = (LinearLayout) this.findViewById(R.id.ll_loading);
		lv_weibo = (PullToRefreshListView) this.findViewById(R.id.lv_weibo);
		lv_weibo.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				mCurrentState = MORE_NEW;
				createNewTask(mCurrentState, max_id);
			}
		});
		lv_weibo.setOnLoadOldListener(new onLoadOldListener() {

			@Override
			public void onLoadOld() {
				mCurrentState = MORE_OLD;
				createNewTask(mCurrentState, max_id);
			}
		});
		lv_weibo.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Status mStatus = adapter.statuses.get(position - 1);
				Intent mIntent = new Intent(HomeActivity.this,
						DetailWeiboActivity.class);
				mIntent.putExtra("status", mStatus);
				HomeActivity.this.startActivity(mIntent);
			}
		});

		btn_refresh = (Button) this.findViewById(R.id.btn_refresh);
		btn_refresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				btn_refresh.setAnimation(rotateAnimation);
				mCurrentState = MORE_NEW;
				createNewTask(mCurrentState, max_id);
			}
		});
		btn_say = (Button) this.findViewById(R.id.btn_say);
		btn_say.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(HomeActivity.this,
						NewWeiboActivity.class);
				startActivityForResult(intent, 0);
			}
		});
		lv_weibo.setCacheColorHint(Color.WHITE);
		// 创建新任务
		createNewTask(INITIATE, max_id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refresh(Object... params) {
		mStatuses = (ArrayList<Status>) params[0];
		if (mStatuses != null) {
			max_id = Long.parseLong(mStatuses.get(mStatuses.size() - 1).getMid()) - 1;
			switch (mCurrentState) {
				case INITIATE :
					ll_loading.setVisibility(View.GONE);
					adapter = new WeiboAdapter(mStatuses, this);
					lv_weibo.setAdapter(adapter);
					break;
				case MORE_NEW :
					btn_refresh.clearAnimation();
					adapter = new WeiboAdapter(mStatuses, this);
					lv_weibo.setAdapter(adapter);
					lv_weibo.onRefreshComplete();
					break;
				case MORE_OLD :
					adapter.refresh(mStatuses);
					lv_weibo.setSelection(adapter.getCount() - 20);
					lv_weibo.resetFooter();
					// tv_more.setVisibility(View.VISIBLE);
					// ll_loading_more.setVisibility(View.INVISIBLE);
					break;
			}
		}
	}

	/**
	 * 新建任务
	 * 
	 * @param current_state
	 * @param max_id
	 */
	private void createNewTask(int current_state, long max_id) {
		Task mTask = null;
		HashMap<String, Object> mTaskParams = new HashMap<String, Object>();
		switch (current_state) {
			case INITIATE :
				mTaskParams.put("state", INITIATE);
				// MainService.addActivity(this);
				this.startService(new Intent(this, MainService.class));
				break;
			case MORE_NEW :
				mTaskParams.put("state", MORE_NEW);
				break;
			case MORE_OLD :
				mTaskParams.put("state", MORE_OLD);
				mTaskParams.put("max_id", String.valueOf(max_id));
				break;
		}
		mTask = new Task(Task.WEIBO_STATUSES_FRIENDS_TIMELINE, mTaskParams);
		MainService.addTask(mTask);
		MainService.addActivity(this);
	}

}
