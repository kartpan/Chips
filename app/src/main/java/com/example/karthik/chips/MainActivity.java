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

    protected List<Chip> chips;
    protected EditText text;
    protected Context context;
    protected List<String> mentions;
    protected String formattedText;
    protected LinearLayout ll;
    protected ActionBar.LayoutParams lp;
    protected TextView txtMentions;
    protected String textValue;
    protected String htmlValue;
    protected boolean currentlyEditing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chips = new ArrayList<Chip>();
        mentions = new ArrayList<String>();
        textValue = "";
        htmlValue = "";
        currentlyEditing = false;
        ll = (LinearLayout)findViewById(R.id.layout);
        lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        txtMentions = (TextView)findViewById(R.id.mentions);

        text = (EditText)findViewById(R.id.inputText);
        context = this;

        text.addTextChangedListener(new TextWatcher() {


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                String value = "" + s;
                String name = "";
                int initialPosition = 0;
                int position = 0;
                boolean intermediateDelete = false;
                boolean falseString = false;
                String currentText = text.getText().toString();
                String finalString=currentText;

                List<String> names = tokenizer(currentText);

                if (before >= 1 && (before != count)) {

                    if(!names.isEmpty()) {
                        for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            if (!names.contains(mention)) {
                                iter.remove();
                            }
                        }
                    } else if (start >= 1 || (currentText.length() == 0 && !currentlyEditing)) {
                        for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                            iter.next();
                            iter.remove();
                        }
                    }

                    for (Iterator<String> iter = names.listIterator(); iter.hasNext(); ) {
                        String mention = iter.next();
                        if (!mentions.contains(mention)) {
                            if (!mention.equals("N.A.")) {
                                finalString = finalString.replace(mention, "");
                            }
                            intermediateDelete = true;
                        }
                    }

                    Iterator<Chip> iterator = chips.iterator();
                    while (iterator.hasNext()) {
                        Chip currChips = iterator.next();


                        if (!mentions.contains("[" + currChips.getChipText() + "]")) {
                            iterator.remove();
                            currChips.setVisibility(View.GONE);
                        }
                    }
                    if (chips.isEmpty()) {
                        txtMentions.setVisibility(View.GONE);
                    }

                    String difference = StringUtils.difference(value, textValue);
                    position = StringUtils.indexOfDifference(value, textValue);
                    initialPosition = position;

                    if (intermediateDelete && difference.charAt(0) != ']') {

                        for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            finalString = finalString.replace(mention, "<font color=blue>" + mention + "</font>");
                        }

                        currentlyEditing = true;
                        text.setText(Html.fromHtml(finalString.trim()));
                        int cursorPosition = text.length();
                        Log.w("Delete:", "Positions:" + start + ":" + before + ":" + count + ":" + cursorPosition);
                        int pos;
                        if (start < cursorPosition) {
                            pos = start;
                        } else {
                            pos = cursorPosition;
                        }
                        text.setSelection(pos);
                        currentlyEditing = false;

                    } else if (difference.charAt(0) == ']') {

                        while (true) {
                            if (s.charAt(--position) == '[') {
                                break;
                            } else if (position == 0 || s.charAt(position) == ']') {
                                falseString = true;
                                break;
                            }
                            name = name + s.charAt(position);
                        }

                        if (falseString) {
                            name = "";
                        }

                        finalString = currentText.substring(0, position) + currentText.substring(initialPosition, currentText.length());

                        StringBuilder nameBuilder = new StringBuilder(name).reverse();

                        if (!name.isEmpty()) {

                            for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                                String mention = iter.next();
                                finalString = finalString.replace(mention, "<font color=blue>" + mention + "</font>");
                            }

                            currentlyEditing = true;
                            text.setText(Html.fromHtml(finalString.trim()));
                            int cursorPosition = text.length();
                            int pos;
                            if (start < cursorPosition) {
                                pos = start;
                            } else {
                                pos = cursorPosition;
                            }
                            text.setSelection(pos);

                            text.setSelection(pos);
                            currentlyEditing = false;

                            if (!text.getText().toString().contains(nameBuilder.toString())) {
                                Iterator<Chip> iter = chips.iterator();
                                while (iter.hasNext()) {
                                    Chip currChips = iter.next();

                                    if (currChips.getChipText().equals(nameBuilder.toString())) {
                                        iter.remove();
                                        currChips.setVisibility(View.GONE);
                                    }
                                }
                                if (chips.isEmpty()) {
                                    txtMentions.setVisibility(View.GONE);
                                }

                            }

                        }
                    }

                }

                textValue = text.getText().toString();
            }


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

                String textValue = text.getText().toString();

                if (count == 0 && after >= 1 && textValue.length() > 0) {
                    int currPosition = checkMention(textValue, --start);
                    if (currPosition != start) {
                        for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            textValue = textValue.replace(mention, "<font color=blue>" + mention + "</font>");
                        }
                        currentlyEditing = true;
                        text.setText(Html.fromHtml(textValue.trim()));
                        text.setSelection(++currPosition);
                        currentlyEditing = false;

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                text.removeTextChangedListener(this);
                String txt = text.getText().toString();
                if (txt.contains("@")) {

                    PopupMenu popup = new PopupMenu(context, text);
                    popup.getMenuInflater().inflate(R.menu.names, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {


                            String txt = text.getText().toString();
                            formattedText = txt.replace("@", "["+ item.toString() + "]");
                            mentions.add("[" + item.toString() + "]");

                            for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                                String name =  iter.next();
                                formattedText = formattedText.replace(name, "<font color=blue>" + name + "</font>");
                            }

                            text.setText(Html.fromHtml(formattedText.trim()));
                            int position = text.length();
                            text.setSelection(position);
                            currentlyEditing = false;
                            createChip(item.toString());
                            return true;
                        }
                    });
                    popup.show();



                }

                text.addTextChangedListener(this);

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

        Log.w("Delete:", "Values: " + text + ":" + text.length() + ":" + text.charAt(currPosition));

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

        if (txtMentions.getVisibility() == View.GONE) {
            txtMentions.setVisibility(View.VISIBLE);
        }


        final Chip chip = new Chip(this);
        chip.setChipIcon(getResources().getDrawable(R.drawable.img_avatar2));
        chip.setChipText(name);
        chip.setSelectable(true);
        chip.setHasIcon(true);
        chip.setClosable(true);
        chip.setPadding(10,0,10,0);
        chips.add(chip);


        boolean available = false;
        int count = 0;

        for (final Chip allChips: chips) {

            if(allChips.getChipText().equals(name)) {
                count++;
                available=true;
            }

            allChips.setOnCloseClickListener(new OnCloseClickListener() {
                @Override
                public void onCloseClick(View v) {
                    allChips.setVisibility(View.GONE);
                    String txt = text.getText().toString();

                    txt = txt.replace("["+allChips.getChipText()+"]", "");

                    for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                        String name =  iter.next();
                        txt = txt.replace(name, "<font color=blue>" + name + "</font>");
                    }

                    currentlyEditing = true;
                    text.setText(Html.fromHtml(txt.trim()));
                    int position = text.length();

                    Iterator<Chip> iter = chips.iterator();

                    while (iter.hasNext()) {
                        Chip currChips = iter.next();

                        if(currChips.getChipText().equals(allChips.getChipText())) {
                            iter.remove();
                        }
                    }

                    if (chips.isEmpty()) {
                        txtMentions.setVisibility(View.GONE);
                    }


                    text.setSelection(position);
                    currentlyEditing = false;

                }
            });
        }

        if(!available || count == 1) {
            chip.setVisibility(View.VISIBLE);
            ll.addView(chip, lp);
        }

    }

}
