package com.fbapp.sheng.facebookpagemanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PublishPostsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class PublishPostsFragment extends Fragment {
    private String TAG = "PublishPostsFragment";
    private OnFragmentInteractionListener mListener;
    Calendar calendar = Calendar.getInstance();


    private EditText dateField;
    private EditText timeField;
    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDateField();
        }
    };
    TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            updateTimeField();
        }
    };

    public PublishPostsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
            final View dialogLayout = inflater.inflate(R.layout.create_posts_dialog, container, false);
            final CheckBox isUnpublished = (CheckBox) dialogLayout.findViewById(R.id.checkbox_is_unpublished);
            final CheckBox isScheduled = (CheckBox) dialogLayout.findViewById(R.id.checkbox_schedule_post);
            dateField = (EditText) dialogLayout.findViewById(R.id.date_text_field);
            timeField = (EditText) dialogLayout.findViewById(R.id.time_text_field);


            isUnpublished.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        isScheduled.setEnabled(true);
                    }
                    else {
                        isScheduled.setEnabled(false);
                    }
                }
            });

            isScheduled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked) {
                        dateField.setEnabled(true);
                        timeField.setEnabled(true);
                    }
                    else {
                        dateField.setEnabled(false);
                        timeField.setEnabled(false);
                    }
                }
            });

            dateField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DatePickerDialog(getContext(),dateSetListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)).show();
                }
            });

            timeField.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(getContext(),timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
                }
            });

            Button publishButton = (Button) dialogLayout.findViewById(R.id.button_post);
            publishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText postText = (EditText) dialogLayout.findViewById(R.id.create_post_text);
                    String pageAccessToken = getActivity().getSharedPreferences("PagePreference", Context.MODE_PRIVATE).getString("access_token", "none");
                    if(pageAccessToken == "none") {
                        Toast.makeText(getActivity(), "Error acquiring access token", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Bundle parameters = new Bundle();
                        parameters.putString("message", postText.getText().toString());
                        parameters.putString("access_token", pageAccessToken);
                        if (isUnpublished.isChecked()) {
                            parameters.putBoolean("published", false);
                        }
                        if (isScheduled.isEnabled() && isScheduled.isChecked()) {
                            String date_string = dateField.getText().toString();
                            if (date_string.isEmpty()) {
                                // Error
                                Toast.makeText(getActivity(), "Date cannot be empty", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String time_string = timeField.getText().toString();
                            DateFormat formatter;
                            Date convertibleDate;
                            if (time_string.isEmpty()) {
                                formatter = new SimpleDateFormat("MM/dd/yy");
                                try {
                                    convertibleDate = (Date) formatter.parse(date_string);
                                    parameters.putLong("scheduled_publish_time", convertibleDate.getTime() / 1000L);
                                } catch (ParseException pe) {
                                    pe.printStackTrace();
                                }
                            } else {
                                formatter = new SimpleDateFormat("MM/dd/yy h:mm a");
                                try {
                                    convertibleDate = (Date) formatter.parse(date_string + " " + time_string);
                                    parameters.putLong("scheduled_publish_time", convertibleDate.getTime() / 1000L);
                                } catch (ParseException pe) {
                                    pe.printStackTrace();
                                }
                            }
                        }
                        pushContent(parameters);
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            });

            Button closeButton = (Button) dialogLayout.findViewById(R.id.button_close);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        return dialogLayout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void pushContent(final Bundle parameters) {
        String pageId = getActivity().getSharedPreferences("PagePreference", Context.MODE_PRIVATE).getString("page_id", "none");
        if (pageId == "none") {
            Toast.makeText(getActivity(), "Error acquiring page_id", Toast.LENGTH_SHORT).show();
        }
        else {
            GraphRequest request = new GraphRequest(AccessToken.getCurrentAccessToken(),
                    "/" + pageId + "/feed",
                    parameters,
                    HttpMethod.POST,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse response) {
                            Log.v(TAG, response.toString());
                        }

                    });
            request.executeAsync();
        }
    }

    private void updateDateField() {
        String format = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        dateField.setText(sdf.format(calendar.getTime()));
    }

    private void updateTimeField() {
        String format = "h:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        timeField.setText(sdf.format(calendar.getTime()));
    }
}
