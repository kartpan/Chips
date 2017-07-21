package com.example.karthik.chips;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
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
    protected int iPreviousLength;
    protected boolean bBackSpace;
    protected boolean bIntermediateDelete;


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
        lpParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);

        tvMentions = (TextView) findViewById(R.id.mentions);
        etInputText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String strInput = s.toString();
                String strName = "";
                int intStartMentionPosition = 0;
                int intEndMentionPosition = 0;
                bIntermediateDelete = false;
                boolean bFalseString = false;
                String currentText = etInputText.getText().toString();
                String finalString = currentText;

                // Get the mentioned names within the []
                List<String> names = tokenizer(currentText);

                if (before >= 1 && (before != count)) {

                    // Remove all the mentioned names that are not available in the names from
                    // entered text
                    String strHolder = "";

                    if (!names.isEmpty()) {

                        strHolder = iterateCollection(getResources().getString(R.string.Type_A),
                                strHolder, strMentionsCollection, names);

                    } else if (start >= 1 || (currentText.length() == 0 && !bCurrentlyEditing)) {

                        strHolder = iterateCollection(getResources().getString(R.string.Type_B),
                                strHolder, strMentionsCollection, names);

                    }

                    // Check if the names retrieved in the text matches with the mentioned names and
                    // if not remove the name from the input

                    finalString = iterateCollection(getResources().getString(R.string.Type_C),
                            finalString, names, names);


                    //Remove all the chips not in the mentioned names list
                    removeChips(null, true);

                    // If chips collection is empty remove the linear layout to hold the chips
                    if (cpChipsCollection.isEmpty()) {
                        tvMentions.setVisibility(View.GONE);
                    }

                    //
                    String difference = StringUtils.difference(strInput, strInputValue);
                    intStartMentionPosition = StringUtils.indexOfDifference(strInput,
                            strInputValue);
                    intEndMentionPosition = intStartMentionPosition;

                    // If a part of the name in the mentioned list is removed then format the input
                    // and update the Edit text field.
                    if (bIntermediateDelete && (difference.isEmpty() || difference.charAt(0) != ']')) {

                        finalString = iterateCollection(getResources().getString(R.string.Type_D),
                                finalString, strMentionsCollection, names);

                        formatEditText(finalString, start, 0);
                    }
                    // If user deletes the ] part of the mentioned name then delete the whole name
                    // and update the Edit text field.
                    else if (difference.charAt(0) == ']') {
                        while (true) {
                            if (s.charAt(--intStartMentionPosition) == '[') {
                                break;
                            } else if (intStartMentionPosition == 0 ||
                                    s.charAt(intStartMentionPosition) == ']') {
                                bFalseString = true;
                                break;
                            }
                            strName = strName + s.charAt(intStartMentionPosition);
                        }

                        if (bFalseString) {
                            strName = "";
                        }

                        finalString = currentText.substring(0, intStartMentionPosition) +
                                currentText.substring(intEndMentionPosition, currentText.length());

                        StringBuilder nameBuilder = new StringBuilder(strName).reverse();

                        if (!strName.isEmpty()) {

                            finalString = iterateCollection(getResources().getString(R.string.Type_D),
                                    finalString, strMentionsCollection, names);

                            formatEditText(finalString, start, 0);

                            // Remove the associated chips with the deleted mentioned name
                            if (!etInputText.getText().toString().contains(nameBuilder.toString())) {
                                removeChips(nameBuilder.toString(), false);
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


                iPreviousLength = s.length();
                String textValue = etInputText.getText().toString();

                // User try to type inside the mentioned name then move the cursor position to end
                // of the mentioned name
                if (count == 0 && after >= 1 && textValue.length() > 0) {
                    int currPosition = checkMention(textValue, --start);
                    if (currPosition != start) {
                        textValue = iterateCollection(getResources().getString(R.string.Type_D),
                                textValue, strMentionsCollection, null);

                        formatEditText(textValue, 0, currPosition + 1);

                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                etInputText.removeTextChangedListener(this);
                bBackSpace = iPreviousLength >= s.length();

                // If user try to type [ or ] then doesn't allow it since those characters are for
                // mentioned name delimiters
                if ((!bBackSpace) && ((s.charAt(s.length() - 1) == '[') ||
                        (s.charAt(s.length() - 1) == ']'))) {
                    s.replace(s.length() - 1, s.length(), "");
                }

                // Listen for character @ to shop popup menu with names to mention and format the
                // input text with mentioned name in blue color surround by []
                String txt = etInputText.getText().toString();
                if (txt.contains("@")) {

                    PopupMenu popup = new PopupMenu(cxtContext, etInputText);
                    popup.getMenuInflater().inflate(R.menu.names, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {


                        @Override
                        // Upon user choose a name from the popup menu, format the input text
                        public boolean onMenuItemClick(MenuItem item) {

                            String txt = etInputText.getText().toString();
                            strFormattedInputText = txt.replace("@", "[" + item.toString() + "]");
                            strMentionsCollection.add("[" + item.toString() + "]");

                            strFormattedInputText = iterateCollection(
                                    getResources().getString(R.string.Type_D),
                                    strFormattedInputText, strMentionsCollection, null);

                            formatEditText(strFormattedInputText, 0, 0);

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

    // Custom function to tokenize the sting inside [] to retrieve the mentioned names
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

    // Custom function to check if the current position is within mentione name and if so return
    // the name's ending position
    public int checkMention(String text, int currPosition) {

        int tobePosition = currPosition;
        int mentionStart = currPosition;
        int mentionEnd = currPosition;
        boolean rightHasBracket = false;
        boolean leftHasBracket = false;

        // Look for [ to the right of the cursor and set true if so
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

        // Look for ] to the left of the cursor and set true if so
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

        // if right and left side has [ & ] then return the end position of ]
        if (leftHasBracket && rightHasBracket) {
            tobePosition = mentionEnd;
        } else {
            tobePosition = currPosition;
        }

        return tobePosition;
    }

    // Custom function to create a chip inside the linear layout
    public void createChip(String strName) {

        // Variables initialization
        boolean bAvailable = false;
        int iCount = 0;
        final Chip cpChip = new Chip(this);


        if (tvMentions.getVisibility() == View.GONE) {
            tvMentions.setVisibility(View.VISIBLE);
        }

        configureChip(strName, cpChip);
        cpChipsCollection.add(cpChip);

        for (final Chip cpAllChips : cpChipsCollection) {

            if (cpAllChips.getChipText().equals(strName)) {
                iCount++;
                bAvailable = true;
            }

            cpAllChips.setOnCloseClickListener(new OnCloseClickListener() {
                @Override
                public void onCloseClick(View v) {
                    cpAllChips.setVisibility(View.GONE);
                    String txt = etInputText.getText().toString();

                    txt = txt.replace("[" + cpAllChips.getChipText() + "]", "");

                    txt = iterateCollection(getResources().getString(R.string.Type_D),
                            txt, strMentionsCollection, null);

                    formatEditText(txt, 0, 0);

                    removeChips(cpAllChips.getChipText(), false);

                    if (cpChipsCollection.isEmpty()) {
                        tvMentions.setVisibility(View.GONE);
                    }


                }
            });
        }

        if (!bAvailable || iCount == 1) {
            cpChip.setVisibility(View.VISIBLE);
            llLayoutSpec.addView(cpChip, lpParams);
        }

    }

    // To set the Chip's attributes before creation
    public void configureChip(String name, Chip chip) {

        chip.setChipIcon(getResources().getDrawable(R.drawable.img_avatar2));
        chip.setChipText(name);
        chip.setSelectable(true);
        chip.setHasIcon(true);
        chip.setClosable(true);
        chip.setPadding(10, 0, 10, 0);

    }

    // Remove the chips from the view
    public void removeChips(String strChipText, boolean removeNotInMentions) {

        Iterator<Chip> iter = cpChipsCollection.iterator();

        while (iter.hasNext()) {
            Chip currChips = iter.next();

            if (removeNotInMentions) {
                if (!strMentionsCollection.contains("[" + currChips.getChipText() + "]")) {
                    iter.remove();
                    currChips.setVisibility(View.GONE);
                }
            } else if (currChips.getChipText().equals(strChipText)) {
                iter.remove();
                currChips.setVisibility(View.GONE);
            }
        }
    }

    // Remove the chips from the view
    public String iterateCollection(String strType, String input, List<String> strMentions,
                                    List<String> strNames) {
        //remove_mention_not_in_name
        for (Iterator<String> iter = strMentions.listIterator(); iter.hasNext(); ) {
            String strMention = iter.next();

            //remove_mention_not_in_name
            if (strType.equals(getResources().getString(R.string.Type_A))) {
                if (!strNames.contains(strMention)) {
                    iter.remove();
                }
            }
            //remove_all_mention
            else if ((strType.equals(getResources().getString(R.string.Type_B)))) {
                iter.remove();
            }
            //remove_name_not_mention
            else if ((strType.equals(getResources().getString(R.string.Type_C)))) {
                if (!strMentionsCollection.contains(strMention)) {
                    if (!strMention.equals("N.A.")) {
                        input = input.replace(strMention, "");
                    }
                    bIntermediateDelete = true;
                }
            }
            //format_input_text
            else if ((strType.equals(getResources().getString(R.string.Type_D)))) {
                input = input.replace(strMention, "<font color=blue>" + strMention
                        + " </font>");
            }

        }
        return input;
    }

    // Format EditField and apply the text
    public void formatEditText(String input, int start, int position) {

        bCurrentlyEditing = true;
        etInputText.setText(Html.fromHtml(input.trim()));
        int finalPosition = etInputText.length();

        // Calculate the cursor position
        if (position == 0 && start != 0) {
            if (start < finalPosition) {
                finalPosition = start;
            }
        } else if (position == 0 && start == 0) {
            finalPosition = finalPosition;
        } else {
            finalPosition = position;
        }

        // Set the cursor position
        etInputText.setSelection(finalPosition);
        bCurrentlyEditing = false;
    }

}