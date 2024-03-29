/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
package com.hulles.alixia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

import com.hulles.alixia.cayenne.NbestAnswer;
import com.hulles.alixia.cayenne.Nfl6Category;

/**
 * Class _Nfl6Question was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _Nfl6Question extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String NFL6_QUESTION_ID_PK_COLUMN = "nfl6_question_ID";

    public static final Property<String> BEST_ANSWER = Property.create("bestAnswer", String.class);
    public static final Property<String> QUESTION = Property.create("question", String.class);
    public static final Property<String> YAHOO_ID = Property.create("yahooId", String.class);
    public static final Property<List<NbestAnswer>> NBEST_ANSWERS = Property.create("nbestAnswers", List.class);
    public static final Property<Nfl6Category> NFL6CATEGORY = Property.create("nfl6Category", Nfl6Category.class);

    protected String bestAnswer;
    protected String question;
    protected String yahooId;

    protected Object nbestAnswers;
    protected Object nfl6Category;

    public void setBestAnswer(String bestAnswer) {
        beforePropertyWrite("bestAnswer", this.bestAnswer, bestAnswer);
        this.bestAnswer = bestAnswer;
    }

    public String getBestAnswer() {
        beforePropertyRead("bestAnswer");
        return this.bestAnswer;
    }

    public void setQuestion(String question) {
        beforePropertyWrite("question", this.question, question);
        this.question = question;
    }

    public String getQuestion() {
        beforePropertyRead("question");
        return this.question;
    }

    public void setYahooId(String yahooId) {
        beforePropertyWrite("yahooId", this.yahooId, yahooId);
        this.yahooId = yahooId;
    }

    public String getYahooId() {
        beforePropertyRead("yahooId");
        return this.yahooId;
    }

    public void addToNbestAnswers(NbestAnswer obj) {
        addToManyTarget("nbestAnswers", obj, true);
    }

    public void removeFromNbestAnswers(NbestAnswer obj) {
        removeToManyTarget("nbestAnswers", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<NbestAnswer> getNbestAnswers() {
        return (List<NbestAnswer>)readProperty("nbestAnswers");
    }

    public void setNfl6Category(Nfl6Category nfl6Category) {
        setToOneTarget("nfl6Category", nfl6Category, true);
    }

    public Nfl6Category getNfl6Category() {
        return (Nfl6Category)readProperty("nfl6Category");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "bestAnswer":
                return this.bestAnswer;
            case "question":
                return this.question;
            case "yahooId":
                return this.yahooId;
            case "nbestAnswers":
                return this.nbestAnswers;
            case "nfl6Category":
                return this.nfl6Category;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "bestAnswer":
                this.bestAnswer = (String)val;
                break;
            case "question":
                this.question = (String)val;
                break;
            case "yahooId":
                this.yahooId = (String)val;
                break;
            case "nbestAnswers":
                this.nbestAnswers = val;
                break;
            case "nfl6Category":
                this.nfl6Category = val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeObject(this.bestAnswer);
        out.writeObject(this.question);
        out.writeObject(this.yahooId);
        out.writeObject(this.nbestAnswers);
        out.writeObject(this.nfl6Category);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.bestAnswer = (String)in.readObject();
        this.question = (String)in.readObject();
        this.yahooId = (String)in.readObject();
        this.nbestAnswers = in.readObject();
        this.nfl6Category = in.readObject();
    }

}
