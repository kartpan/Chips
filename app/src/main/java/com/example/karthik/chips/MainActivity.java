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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chips = new ArrayList<Chip>();
        mentions = new ArrayList<String>();

        textValue = "";
        htmlValue = "";
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

                if (before >= 1) {

                    if(!names.isEmpty()) {
                        for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            if (!names.contains(mention)) {
                                iter.remove();
                            }
                        }
                    }

                    Log.w("Delete:", "Name:" + names);

                    for (Iterator<String> iter = names.listIterator(); iter.hasNext(); ) {
                        String mention = iter.next();
                        if (!mentions.contains(mention)) {
                            finalString = finalString.replace(mention, "");
                            intermediateDelete = true;
                        }
                    }

                    Iterator<Chip> iterator = chips.iterator();
                    while (iterator.hasNext()) {
                        Chip currChips = iterator.next();

                        Log.w("Delete:", "Chips:" + currChips.getChipText());

                        if (!mentions.contains("[" + currChips.getChipText() + "]")) {
                            iterator.remove();
                            currChips.setVisibility(View.GONE);
                        }
                    }
                    if (chips.isEmpty()) {
                        txtMentions.setVisibility(View.GONE);
                    }

                    if (intermediateDelete) {

                        for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                            String mention = iter.next();
                            finalString = finalString.replace(mention, "<font color=blue>" + mention + "</font>");
                        }

                        text.setText(Html.fromHtml(finalString.trim()));
                        int cursorPosition = text.length();
                        text.setSelection(cursorPosition);

                    } else {

                        String difference = StringUtils.difference(value, textValue);
                        position = StringUtils.indexOfDifference(value, textValue);
                        initialPosition = position;

                        if (difference.charAt(0) == ']') {

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
                        }

                        finalString = currentText.substring(0, position) + currentText.substring(initialPosition, currentText.length());

                        StringBuilder nameBuilder = new StringBuilder(name).reverse();

                        if (!name.isEmpty()) {

                            for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                                String mention = iter.next();
                                finalString = finalString.replace(mention, "<font color=blue>" + mention + "</font>");
                            }

                            text.setText(Html.fromHtml(finalString.trim()));
                            int cursorPosition = text.length();
                            text.setSelection(cursorPosition);

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

                            text.setText("");
                            for (Iterator<String> iter = mentions.listIterator(); iter.hasNext(); ) {
                                String name =  iter.next();
                                formattedText = formattedText.replace(name, "<font color=blue>" + name + "</font>");
                            }
                            text.setText(Html.fromHtml(formattedText.trim()));
                            int position = text.length();
                            text.setSelection(position);
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

        while (count < text.length()) {
            if (text.charAt(count) == '[') {
                nameDelimiter = true;
            } else if (text.charAt(count) == ']') {
                nameDelimiter = false;
                name = name + "]";
                names.add(name);
                name = "";
            }

            if (nameDelimiter) {
                name = name + text.charAt(count);
            }
            count++;
        }
        return names;
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

                }
            });
        }

        if(!available || count == 1) {
            chip.setVisibility(View.VISIBLE);
            ll.addView(chip, lp);
        }

    }

}
