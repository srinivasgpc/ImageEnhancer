package com.softa.imageenhancer;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends FragmentActivity {

	final static int SELECT_IMAGE = 10;
	private ImageView beforeImageView, afterImageView;
	private Bitmap theImage;
	private Button loadButton;
	private Button improveButton;
	private ImageEnhancer selectedEnhancer;
	private int selectedConfiguration;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		selectedEnhancer = getEnhancers().get(0); // Here we choose which enhancer to use

		//// print how much memory the app is allowed to use on this device
		Runtime rt = Runtime.getRuntime();
		long maxMemory = rt.maxMemory();
		Log.d("onCreate", "maxMemory:" + Long.toString(maxMemory));
		//////////////////////////////////

		loadButton = (Button) findViewById(R.id.load_button);
		improveButton = (Button) findViewById(R.id.improve_button);
		improveButton.setVisibility(View.INVISIBLE);

		beforeImageView = (ImageView) findViewById(R.id.imageview1);
		afterImageView = (ImageView) findViewById(R.id.imageview2);
		
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setTitle("Processing image");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgressNumberFormat(null);

		loadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(Intent.createChooser(intent, "Zelect image:"), SELECT_IMAGE);

			}
		});

		beforeImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(loadButton.getVisibility() == View.VISIBLE)
				    loadButton.setVisibility(View.INVISIBLE);
				else
					loadButton.setVisibility(View.VISIBLE);

			}
		});

		improveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				FragmentManager fm = getSupportFragmentManager();
				new ConfigurationDialog().show(fm, "configuration_dialog");
			}
		});

		afterImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(improveButton.getVisibility() == View.VISIBLE)
					improveButton.setVisibility(View.INVISIBLE);
				else
					improveButton.setVisibility(View.VISIBLE);

			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			
			ParcelFileDescriptor parcelFileDescriptor;
			try {
				parcelFileDescriptor = getContentResolver().openFileDescriptor(
						selectedImage, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor
						.getFileDescriptor();
				theImage = BitmapFactory.decodeFileDescriptor(fileDescriptor);
				
				// get screen width and scale image to fit 
				int activityWidth = getWindow().getDecorView().getWidth();
				int activityHeight = getWindow().getDecorView().getHeight() - getStatusBarHeight();
				int width;
				int height;
				if (theImage.getWidth() > theImage.getHeight()) { // Landscape
					width = activityWidth;
					height = theImage.getHeight() * width / theImage.getWidth(); //Keep aspect ratio
				} else {
					height = activityHeight / 2;
					width = theImage.getWidth() * height / theImage.getHeight();
				}
				Log.d("DEBUG","creating scaled BITMAP,width x height "+width+" "+height);
				theImage = Bitmap.createScaledBitmap(theImage, width,
						height, false);
				parcelFileDescriptor.close();
				beforeImageView.setImageBitmap(theImage);
				improveButton.setVisibility(View.VISIBLE);
				loadButton.setVisibility(View.INVISIBLE);  //Hide the loadButton to not obscure the original pic.
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private int getStatusBarHeight() {
	    int result = 0;
	    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
	    if (resourceId > 0) {
	        result = getResources().getDimensionPixelSize(resourceId);
	    }
	    return result;
	}
	
	private class ImproveImageTask extends AsyncTask<Bitmap, Integer, Bitmap> {

		protected Bitmap doInBackground(Bitmap... urls) {
			
			new Thread(
					  new Runnable() {

					      public void run() {
					         while (progressDialog.isShowing()) {
					        	 publishProgress();
					        	 try {
									Thread.sleep(200);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
					         }
					      }
					}).start();

			return selectedEnhancer.enhanceImage(theImage, selectedConfiguration);

		}

		protected void onProgressUpdate(Integer... progress) {
			
			progressDialog.setProgress(selectedEnhancer.getProgress());

		}

		protected void onPostExecute(Bitmap result) {
			afterImageView.setImageBitmap(result);
			progressDialog.dismiss();

		}
	}
	
	private List<ImageEnhancer> getEnhancers() {
		ArrayList<ImageEnhancer> enhancers = new ArrayList<ImageEnhancer>();
		
		enhancers.add(new TestEnhancer()); // Here below additional enhancers can be added
		return enhancers;
	}
	
	public class ConfigurationDialog extends DialogFragment {
		
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.select_configuration).setItems(
					selectedEnhancer.getConfigurationOptions(),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							selectedConfiguration = which;
							progressDialog.setProgress(0);
							progressDialog.show();
							new ImproveImageTask().execute(theImage);

						}
		    });
		    return builder.create();
		}
	}

}
