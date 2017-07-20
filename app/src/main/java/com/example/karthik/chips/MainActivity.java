package com.example.karthik.chips;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.robertlevonyan.views.chip.Chip;
import com.robertlevonyan.views.chip.OnCloseClickListener;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Public variables

    protected List<Chip> cpChipsCollection;
    protected EditText etInputText;
    protected Context cxtContext;
    protected List<String> strMentionsCollection;
    protected String strFormattedInputText;
    protected LinearLayout llLayoutSpec;
    protected ActionBar.LayoutParams lpParams;
    protected TextView tvMentions;
    protected String strInputValue;
    protected boolean bCurrentlyEditing;
    private int mPreviousLength;
    private boolean mBackSpace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Global variables initialization
        cxtContext = this;
        cpChipsCollection = new ArrayList<Chip>();
        strMentionsCollection = new ArrayList<String>();
        bCurrentlyEditing = false;
        etInputText = (EditText) findViewById(R.id.inputText);
        strInputValue = "";

        //Linear Layout for chips display about EditText input
        llLayoutSpec = (LinearLayout) findViewById(R.id.layout);
        lpParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);

        tvMentions = (TextView) findViewById(R.id.mentions);
        etInputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String strInput = s.toString();
                String strName = "";
                int intStartMentionPosition = 0;
                int intEndMentionPosition = 0;
                boolean bIntermediateDelete = false;
                boolean bFalseString = false;
                String currentText = etInputText.getText().toString();
                String finalString=currentText;

                List<String> names = tokenizer(currentText);

                if (before >= 1 && (before != count)) {
                    if(!names.isEmpty()) {
                        for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            if (!names.contains(mention)) {
                                iter.remove();
                            }
                        }
                    } else if (start >= 1 || (currentText.length() == 0 && !bCurrentlyEditing)) {
                        for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                            iter.next();
                            iter.remove();
                        }
                    }

                    for (Iterator<String> iter = names.listIterator(); iter.hasNext(); ) {
                        String mention = iter.next();
                        if (!strMentionsCollection.contains(mention)) {
                            if (!mention.equals("N.A.")) {
                                finalString = finalString.replace(mention, "");
                            }
                            bIntermediateDelete = true;
                        }
                    }

                    Iterator<Chip> iterator = cpChipsCollection.iterator();
                    while (iterator.hasNext()) {
                        Chip currChips = iterator.next();


                        if (!strMentionsCollection.contains("[" + currChips.getChipText() + "]")) {
                            iterator.remove();
                            currChips.setVisibility(View.GONE);
                        }
                    }
                    if (cpChipsCollection.isEmpty()) {
                        tvMentions.setVisibility(View.GONE);
                    }

                    String difference = StringUtils.difference(strInput, strInputValue);
                    intStartMentionPosition = StringUtils.indexOfDifference(strInput, strInputValue);
                    intEndMentionPosition = intStartMentionPosition;

                    if (bIntermediateDelete && difference.charAt(0) != ']') {
                        for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            finalString = finalString.replace(mention, "<font color=blue>" + mention + "</font>");
                        }

                        bCurrentlyEditing = true;
                        etInputText.setText(Html.fromHtml(finalString.trim()));
                        int cursorPosition = etInputText.length();
                        int pos;
                        if (start < cursorPosition) {
                            pos = start;
                        } else {
                            pos = cursorPosition;
                        }
                        etInputText.setSelection(pos);
                        bCurrentlyEditing = false;

                    } else if (difference.charAt(0) == ']') {
                        while (true) {
                            if (s.charAt(--intStartMentionPosition) == '[') {
                                break;
                            } else if (intStartMentionPosition == 0 || s.charAt(intStartMentionPosition) == ']') {
                                bFalseString = true;
                                break;
                            }
                            strName = strName + s.charAt(intStartMentionPosition);
                        }

                        if (bFalseString) {
                            strName = "";
                        }

                        finalString = currentText.substring(0, intStartMentionPosition) + currentText.substring(intEndMentionPosition, currentText.length());

                        StringBuilder nameBuilder = new StringBuilder(strName).reverse();

                        if (!strName.isEmpty()) {

                            for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                                String mention = iter.next();
                                finalString = finalString.replace(mention, "<font color=blue>" + mention + "</font>");
                            }

                            bCurrentlyEditing = true;
                            etInputText.setText(Html.fromHtml(finalString.trim()));
                            int cursorPosition = etInputText.length();
                            int pos;
                            if (start < cursorPosition) {
                                pos = start;
                            } else {
                                pos = cursorPosition;
                            }
                            etInputText.setSelection(pos);

                            etInputText.setSelection(pos);
                            bCurrentlyEditing = false;

                            if (!etInputText.getText().toString().contains(nameBuilder.toString())) {
                                Iterator<Chip> iter = cpChipsCollection.iterator();
                                while (iter.hasNext()) {
                                    Chip currChips = iter.next();

                                    if (currChips.getChipText().equals(nameBuilder.toString())) {
                                        iter.remove();
                                        currChips.setVisibility(View.GONE);
                                    }
                                }
                                if (cpChipsCollection.isEmpty()) {
                                    tvMentions.setVisibility(View.GONE);
                                }

                            }

                        }
                    }

                }

                strInputValue = etInputText.getText().toString();
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {


                mPreviousLength = s.length();
                String textValue = etInputText.getText().toString();

                if (count == 0 && after >= 1 && textValue.length() > 0) {
                    int currPosition = checkMention(textValue, --start);
                    if (currPosition != start) {
                        for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            textValue = textValue.replace(mention, "<font color=blue>" + mention + "</font>");
                        }
                        bCurrentlyEditing = true;
                        etInputText.setText(Html.fromHtml(textValue.trim()));
                        etInputText.setSelection(++currPosition);
                        bCurrentlyEditing = false;

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etInputText.removeTextChangedListener(this);

                Log.w("Delete:", "Length:" + mPreviousLength + ":" + s.length());

                mBackSpace = mPreviousLength >= s.length();

                if ((!mBackSpace) && ((s.charAt(s.length() - 1) == '[') || (s.charAt(s.length() - 1) == ']'))) {
                    s.replace(s.length() - 1, s.length(), "");
                }

                String txt = etInputText.getText().toString();
                if (txt.contains("@")) {

                    PopupMenu popup = new PopupMenu(cxtContext, etInputText);
                    popup.getMenuInflater().inflate(R.menu.names, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            
                            String txt = etInputText.getText().toString();
                            strFormattedInputText = txt.replace("@", "[" + item.toString() + "]");
                            strMentionsCollection.add("[" + item.toString() + "]");

                            for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                                String name =  iter.next();
                                strFormattedInputText = strFormattedInputText.replace(name, "<font color=blue>" + name + " </font>");
                            }

                            etInputText.setText(Html.fromHtml(strFormattedInputText.trim()));
                            int position = etInputText.length();
                            etInputText.setSelection(position);
                            bCurrentlyEditing = false;
                            createChip(item.toString());
                            return true;
                        }
                    });
                    popup.show();



                }

                etInputText.addTextChangedListener(this);

            }
        });

    }

    public List<String> tokenizer(String text) {

        List<String> names = new ArrayList<String>();
        String name = "";

        int count = 0;
        boolean nameDelimiter = false;
        boolean entered = false;

        while (count < text.length()) {
            if (text.charAt(count) == '[' && !nameDelimiter) {
                nameDelimiter = true;
                entered = true;
            } else if (text.charAt(count) == '[' && nameDelimiter) {
                nameDelimiter = true;
                names.add("N.A.");
                name = "";

            } else if (text.charAt(count) == ']') {
                nameDelimiter = false;
                if (entered) {
                    name = name + "]";
                    names.add(name);
                    entered = false;
                } else {
                    names.add("N.A.");
                }
                name = "";
            }

            if (nameDelimiter) {
                name = name + text.charAt(count);
            }
            count++;
        }
        if (nameDelimiter) {
            names.add("N.A.");
        }
        return names;
    }

    public int checkMention(String text, int currPosition) {

        int tobePosition = currPosition;
        int mentionStart = currPosition;
        int mentionEnd = currPosition;
        boolean rightHasBracket = false;
        boolean leftHasBracket = false;

        while (mentionStart >= 0) {
            if (text.charAt(mentionStart) == '[') {
                rightHasBracket = true;
                break;
            } else if (text.charAt(mentionStart) == ']' && (mentionStart != currPosition)) {
                rightHasBracket = false;
                break;
            }
            mentionStart--;
        }

        while (mentionEnd < text.length()) {
            if (text.charAt(mentionEnd) == ']') {
                leftHasBracket = true;
                break;
            } else if (text.charAt(mentionEnd) == '[' && (mentionEnd != currPosition)) {
                rightHasBracket = false;
                break;
            }
            mentionEnd++;
        }

        if (leftHasBracket && rightHasBracket) {
            tobePosition = mentionEnd;
        } else {
            tobePosition = currPosition;
        }

        return tobePosition;
    }

    public void createChip(String name) {

        if (tvMentions.getVisibility() == View.GONE) {
            tvMentions.setVisibility(View.VISIBLE);
        }


        final Chip chip = new Chip(this);
        chip.setChipIcon(getResources().getDrawable(R.drawable.img_avatar2));
        chip.setChipText(name);
        chip.setSelectable(true);
        chip.setHasIcon(true);
        chip.setClosable(true);
        chip.setPadding(10,0,10,0);
        cpChipsCollection.add(chip);


        boolean available = false;
        int count = 0;

        for (final Chip allChips : cpChipsCollection) {

            if(allChips.getChipText().equals(name)) {
                count++;
                available=true;
            }

            allChips.setOnCloseClickListener(new OnCloseClickListener() {
                @Override
                public void onCloseClick(View v) {
                    allChips.setVisibility(View.GONE);
                    String txt = etInputText.getText().toString();

                    txt = txt.replace("["+allChips.getChipText()+"]", "");

                    for (Iterator<String> iter = strMentionsCollection.listIterator(); iter.hasNext(); ) {
                        String name =  iter.next();
                        txt = txt.replace(name, "<font color=blue>" + name + "</font>");
                    }

                    bCurrentlyEditing = true;
                    etInputText.setText(Html.fromHtml(txt.trim()));
                    int position = etInputText.length();

                    Iterator<Chip> iter = cpChipsCollection.iterator();

                    while (iter.hasNext()) {
                        Chip currChips = iter.next();

                        if(currChips.getChipText().equals(allChips.getChipText())) {
                            iter.remove();
                        }
                    }

                    if (cpChipsCollection.isEmpty()) {
                        tvMentions.setVisibility(View.GONE);
                    }


                    etInputText.setSelection(position);
                    bCurrentlyEditing = false;

                }
            });
        }

        if(!available || count == 1) {
            chip.setVisibility(View.VISIBLE);
            llLayoutSpec.addView(chip, lpParams);
        }

    }

}
