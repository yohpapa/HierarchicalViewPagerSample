package com.yohpapa.research.viewpagersample;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.viewpagerindicator.TitlePageIndicator;

public class MainActivity extends FragmentActivity {
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private boolean isJustAfterResume = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final TestFragmentPagerAdapter adapter = new TestFragmentPagerAdapter(this);
		
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);

        TitlePageIndicator indicator = (TitlePageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				TestFragmentPage current = (TestFragmentPage)adapter.getItem(position);
				current.onPageSelected(isJustAfterResume);
				isJustAfterResume = false;
				
				// Store the position of current page
				PrefUtils.setInt(MainActivity.this, R.string.pref_last_tab, position);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
			
			@Override
			public void onPageScrollStateChanged(int state) {}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		isJustAfterResume = true;
		
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		PagerAdapter adapter = pager.getAdapter();
		
		// Resume the last page
		int lastPage = PrefUtils.getInt(this, R.string.pref_last_tab, 0);
		if(lastPage < adapter.getCount()) {
			pager.setCurrentItem(lastPage);
		}
	}
	
	public boolean isSelectedTab(String title) {
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		TestFragmentPagerAdapter adapter = (TestFragmentPagerAdapter)pager.getAdapter();
		int selectedNo = pager.getCurrentItem();
		String pageTitle = String.valueOf(adapter.getPageTitle(selectedNo));
		return pageTitle.equals(title);
	}
	
	public void printBackStack() {
		FragmentManager manager = getSupportFragmentManager();
		int numFragments = manager.getBackStackEntryCount();
		
		Log.d(TAG, "MainActivity: numFragments: " + numFragments);
		
		int countFragments = 0;
		ViewPager pager = (ViewPager)findViewById(R.id.pager);
		FragmentPagerAdapter adapter = (FragmentPagerAdapter)pager.getAdapter();
		for(int i = 0; i < adapter.getCount(); i ++) {
			Fragment fragment = adapter.getItem(i);
			manager = fragment.getChildFragmentManager();
			Log.d(TAG, "MainActivity:" + "ChildFragment[" + i +"]" + "numFragments: " + manager.getBackStackEntryCount());
			countFragments += manager.getBackStackEntryCount();
		}
		
		if(countFragments > 0)
			return;
	}
	
	private class TestFragmentPagerAdapter extends FragmentPagerAdapter {

		private String[] titles = null;
		private int[] ids = {
			R.string.page_title_1,
			R.string.page_title_2,
			R.string.page_title_3,
			R.string.page_title_4,
			R.string.page_title_5,
		};
		private Fragment[] fragments = null;
		
		public TestFragmentPagerAdapter(FragmentActivity activity) {
			super(activity.getSupportFragmentManager());
			
			FragmentManager manager = activity.getSupportFragmentManager();
			
			titles = new String[ids.length];
			fragments = new Fragment[ids.length];
			for(int i = 0; i < ids.length; i ++) {
				titles[i] = activity.getString(ids[i]);
				
				Fragment fragment = manager.findFragmentByTag(titles[i] + ":0");
				if(fragment == null) {
					fragment = TestFragmentPage.newInstance(titles[i], ids[i]);
				}
				fragments[i] = fragment;
			}
		}

		@Override
		public Fragment getItem(int position) {
			return fragments[position];
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}
	}
	
	public static class TestFragmentPage extends Fragment {
		
		public static TestFragmentPage newInstance(String title, int fragmentId) {
			
			Bundle arguments = new Bundle();
			arguments.putString("KEY", title);
			arguments.putInt("FRAGMENT", fragmentId);
			
			TestFragmentPage fragment = new TestFragmentPage();
			fragment.setArguments(arguments);
			
			return fragment;
		}
		
		private String title;
		private int fragmentId;
		private boolean isFirstSelected = false;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			Bundle parameters = getArguments();
			if(parameters != null) {
				title = parameters.getString("KEY");
				fragmentId = parameters.getInt("FRAGMENT");
			}
		}
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
			Context context = getActivity();
			
			LinearLayout base = new LinearLayout(context);
			base.setOrientation(LinearLayout.VERTICAL);
			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			base.setLayoutParams(params);
			
	        TextView text = new TextView(context);
	        text.setId(R.string.title);
	        text.setGravity(Gravity.CENTER);
	        text.setText("Page: " + title);
	        text.setTextSize(20 * getResources().getDisplayMetrics().density);
	        text.setPadding(20, 20, 20, 20);
	        params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	        text.setLayoutParams(params);

	        FrameLayout layout = new FrameLayout(getActivity());
	        layout.setId(fragmentId);
	        params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
	        params.weight = 1;
	        layout.setLayoutParams(params);
	        layout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					FragmentManager manager = getChildFragmentManager();
					FragmentTransaction transaction = manager.beginTransaction();
					String tag = title + ":0";
					transaction.replace(fragmentId, TestFragment.newInstance(title, 0, fragmentId, manager), tag);
					transaction.addToBackStack(tag);
					transaction.commit();
				}
			});
	        
	        base.addView(text);
	        base.addView(layout);

	        return base;
	    }
		
		public void onPageSelected(boolean isInitial) {
			if(isInitial) {
				isFirstSelected = true;
			} else {
				setupFocus();
			}
		}
		
		public void onResume() {
			super.onResume();
			
			if(isFirstSelected) {
				setupFocus();
				isFirstSelected = false;
			}
		}
		
		private void setupFocus() {

			FragmentManager manager = getChildFragmentManager();
			int numFragments = manager.getBackStackEntryCount();
			if(numFragments <= 0) {
				View view = getView();
				view.setFocusableInTouchMode(true);
				view.requestFocus();
				view.setOnKeyListener(new View.OnKeyListener() {
					@Override
					public boolean onKey(View view, int keyCode, KeyEvent event) {
						if(keyCode != KeyEvent.KEYCODE_BACK)
							return false;
						
						if(event.getAction() == KeyEvent.ACTION_UP) {
							getActivity().finish();
						}
						return true;
					}
				});
				return;
			}
			
			FragmentManager.BackStackEntry entry = manager.getBackStackEntryAt(numFragments - 1);
			if(entry == null)
				return;
			String name = entry.getName();
			if(name == null)
				return;
			
			TestFragment current = (TestFragment)manager.findFragmentByTag(name);
			if(current != null) {
				current.resumeFocus();
			}
		}
	}
	
	public static class TestFragment extends Fragment {
		static TestFragment newInstance(String title, int depth, int fragmentId, FragmentManager manager) {
			
			Bundle arguments = new Bundle();
			arguments.putString("TITLE", title);
			arguments.putInt("DEPTH", depth);
			arguments.putInt("FRAGMENT", fragmentId);
			
			TestFragment fragment = new TestFragment();
			fragment.setArguments(arguments);
			fragment.setPageFragmentManager(manager);
			
			return fragment;
		}
		
		private String title;
		private int depth;
		private int fragmentId;
		private FragmentManager manager;
		
		public void setPageFragmentManager(FragmentManager manager) {
			this.manager = manager;
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			Bundle arguments = getArguments();
			if(arguments != null) {
				title = arguments.getString("TITLE");
				depth = arguments.getInt("DEPTH");
				fragmentId = arguments.getInt("FRAGMENT");
			}
		}
		
		private static final int[] colors = {
			Color.RED,
			Color.BLUE,
			Color.GREEN,
		};
		
		@Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	        FrameLayout layout = new FrameLayout(getActivity());
	        layout.setId(fragmentId);
	        layout.setBackgroundColor(colors[depth % colors.length]);
	        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	        layout.setLayoutParams(params);
	        layout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					FragmentTransaction transaction = manager.beginTransaction();
					String tag = title + ":" + (depth + 1);
					transaction.replace(fragmentId, TestFragment.newInstance(title, depth + 1, fragmentId, manager), tag);
					transaction.addToBackStack(tag);
					transaction.commit();
				}
			});
	        
	        return layout;
	    }
		
		@Override
		public void onResume() {
			super.onResume();

			// The top fragment in the selected tab should only be focused.
			// So I should check whether the fragment's tab is selected or not.
			MainActivity parent = (MainActivity)getActivity();
			if(parent.isSelectedTab(title)) {
				resumeFocus();
			}
		}
		
		public void resumeFocus() {
			View view = getView();
			view.setFocusableInTouchMode(true);
			view.requestFocus();
			view.setOnKeyListener(new View.OnKeyListener() {
				@Override
				public boolean onKey(View view, int keyCode, KeyEvent event) {
					if(keyCode != KeyEvent.KEYCODE_BACK)
						return false;
					
					if(event.getAction() == KeyEvent.ACTION_UP) {
						manager.popBackStack();
					}
					return true;
				}
			});
		}
	}
}
