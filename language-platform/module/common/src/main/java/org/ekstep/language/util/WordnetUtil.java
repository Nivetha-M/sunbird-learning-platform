package org.ekstep.language.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.ilimi.graph.dac.enums.RelationTypes;
import com.ilimi.graph.dac.model.Node;
import com.ilimi.graph.dac.model.Relation;

public class WordnetUtil implements IWordnetConstants {
	
	private static WordUtil wordUtil=new WordUtil();
	
    public static String getPosValue(String posTag) {
        return getPosValue(posTag, true);
    }

    public static String getPosValue(String posTag, boolean returnDefault) {
        if (StringUtils.isNotBlank(posTag)) {
            switch (posTag.trim().toLowerCase()) {
            case POS_TAG_NN:
            case POS_TAG_NST:
            case POS_TAG_NNP:
            case POS_CATEGORY_N:
                return POS_NOUN;
            case POS_TAG_PRP:
            case POS_CATEGORY_PN:
                return POS_PRONOUN;
            case POS_TAG_VM:
            case POS_TAG_VAUX:
            case POS_CATEGORY_V:
                return POS_VERB;
            case POS_TAG_JJ:
            case POS_CATEGORY_ADJ:
                return POS_ADJECTIVE;
            case POS_TAG_RB:
                return POS_ADVERB;
            case POS_TAG_CC:
                return POS_CONJUNCTION;
            case POS_TAG_INJ:
                return POS_INTERJECTION;
            case POS_TAG_UNK:
                return null;
            }
        }
        if (returnDefault)
            return posTag;
        else
            return null;
    }

    public static boolean isStandardPOS(String posTag) {
        if (StringUtils.isNotBlank(posTag)) {
            switch (posTag.trim().toLowerCase()) {
            case POS_NOUN:
            case POS_PRONOUN:
            case POS_VERB:
            case POS_ADVERB:
            case POS_ADJECTIVE:
            case POS_CONJUNCTION:
            case POS_PREPOSITION:
            case POS_INTERJECTION:
            case POS_ARTICLE:
                return true;
            }
        }
        return false;
    }
    
    public static void updatePOS(Node node) {
        try {
            String posValue = null;
            String primaryMeaning = (String) node.getMetadata().get(ATTRIB_PRIMARY_MEANING_ID);
            List<Relation> inRels = node.getInRelations();
            if (null != inRels && !inRels.isEmpty()) {
                String synsetPos = null;
                for (Relation rel : inRels) {
                    if (StringUtils.equalsIgnoreCase(rel.getRelationType(), RelationTypes.SYNONYM.relationName())
                            && StringUtils.equalsIgnoreCase(rel.getStartNodeObjectType(), OBJECTTYPE_SYNSET)) {
                        String synsetId = rel.getStartNodeId();
                        if (StringUtils.equalsIgnoreCase(synsetId, primaryMeaning)) {
                            String pos = (String) rel.getStartNodeMetadata().get(ATTRIB_POS);
                            if (StringUtils.isNotBlank(pos)) {
                                synsetPos = pos;
                                break;
                            }
                        } else if (StringUtils.isBlank(synsetPos)) {
                            String pos = (String) rel.getStartNodeMetadata().get(ATTRIB_POS);
                            if (StringUtils.isNotBlank(pos))
                                synsetPos = pos;
                        }
                    }
                }
                if (StringUtils.isNotBlank(synsetPos))
                    synsetPos = posValue;
            }
            Set<String> posTagList = new HashSet<String>();
            Object posTags = (Object) node.getMetadata().get(ATTRIB_POS_TAGS);
            if (null != posTags && !StringUtils.isBlank(posTags.toString())) {
                if (StringUtils.isBlank(posValue)) {
                    if (posTags instanceof String[]) {
                        String[] arr = (String[]) posTags;
                        if (null != arr && arr.length > 0) {
                            for (String str : arr) {
                                String pos = WordnetUtil.getPosValue(str, false);
                                if (StringUtils.isNotBlank(pos)) {
                                    posValue = pos;
                                    break;
                                }
                            }
                        }
                    } else if (posTags instanceof String) {
                        if (StringUtils.isNotBlank(posTags.toString())) {
                            String pos = WordnetUtil.getPosValue(posTags.toString(), false);
                            if (StringUtils.isNotBlank(pos))
                                posValue = pos;
                        }
                    }
                }
            } else {
                Object value = node.getMetadata().get(ATTRIB_POS);
                if (null != value) {
                    if (value instanceof String[]) {
                        String[] arr = (String[]) value;
                        if (null != arr && arr.length > 0) {
                            for (String str : arr)
                                posTagList.add(str.toLowerCase());
                        }
                    } else if (value instanceof String) {
                        if (StringUtils.isNotBlank(value.toString()))
                            posTagList.add(value.toString().toLowerCase());
                    }
                }
            }
            if (StringUtils.isBlank(posValue)) {
                Object posCategories = node.getMetadata().get(ATTRIB_POS_CATEGORIES);
                if (null != posCategories) {
                    if (posCategories instanceof String[]) {
                        String[] arr = (String[]) posCategories;
                        if (null != arr && arr.length > 0) {
                            for (String str : arr) {
                                String pos = WordnetUtil.getPosValue(str, false);
                                if (StringUtils.isNotBlank(pos)) {
                                    posValue = pos;
                                    break;
                                }
                            }
                        }
                    } else if (posCategories instanceof String) {
                        if (StringUtils.isNotBlank(posCategories.toString())) {
                            String pos = WordnetUtil.getPosValue(posCategories.toString(), false);
                            if (StringUtils.isNotBlank(pos))
                                posValue = pos;
                        }
                    }
                }
            }
            if (StringUtils.isNotBlank(posValue)) {
                List<String> list = new ArrayList<String>();
                list.add(posValue);
                node.getMetadata().put(ATTRIB_POS, list);
            } else {
                node.getMetadata().put(ATTRIB_POS, null);
            }
            if (!posTagList.isEmpty())
                node.getMetadata().put(ATTRIB_POS_TAGS, new ArrayList<String>(posTagList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void updateSyllables(Node node) {
    	String word=(String) node.getMetadata().get(ATTRIB_LEMMA);
    	List<String> syllables=wordUtil.buildSyllables("en", word);
    	node.getMetadata().put(ATTRIB_SYLLABLES,syllables);
    }
}
