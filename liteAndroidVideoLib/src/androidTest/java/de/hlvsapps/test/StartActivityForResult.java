/*-----------------------------------------------------------------------------
 - This is a part of AndroidVideoLib.                                         -
 - To see the authors, look at Github for contributors of this file.          -
 -                                                                            -
 - Copyright 2021  The AndroidVideoLib Authors:                               -
 -       https://github.com/hlvs-apps/AndroidVideoLib/blob/master/AUTHORS.md  -
 - Unless otherwise noted, this is                                            -
 - Licensed under the Apache License, Version 2.0 (the "License");            -
 - you may not use this file except in compliance with the License.           -
 - You may obtain a copy of the License at                                    -
 -                                                                            -
 -     http://www.apache.org/licenses/LICENSE-2.0                             -
 -                                                                            -
 - Unless required by applicable law or agreed to in writing, software        -
 - distributed under the License is distributed on an "AS IS" BASIS,          -
 - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   -
 - See the License for the specific language governing permissions and        -
 - limitations under the License.                                             -
 -----------------------------------------------------------------------------*/

package de.hlvsapps.test;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivityForResult extends AppCompatActivity {
    private boolean success =false;
    private boolean activityResultIsReturned = false;

    boolean getSuccess(){
        return success;
    }

    boolean getActivityResultIsReturned(){
        return activityResultIsReturned;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout view=new LinearLayout(this);
        setContentView(view.getRootView());

        this.startButton = new Button(this);
        this.startButton.setId(123457678);
        this.startButton.setText("Start");
        this.startButton.setOnClickListener(onStart);
        view.addView(startButton);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        success=resultCode==RESULT_OK;
        activityResultIsReturned = true;
    }

    private View.OnClickListener onStart = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(StartActivityForResult.this, video_test.class);

            StartActivityForResult.this.startActivityForResult(intent, 123);
        }
    };
    private Button startButton = null;
}