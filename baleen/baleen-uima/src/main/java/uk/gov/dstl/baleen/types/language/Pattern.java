

/* First created by JCasGen Thu Jan 21 11:56:21 GMT 2016 */
package uk.gov.dstl.baleen.types.language;

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import uk.gov.dstl.baleen.types.Base;


/** The text pattern between two annotations (usually entities) which has been processed to be more meaningful than simply the covered text between them
 * Updated by JCasGen Thu Jan 21 11:56:21 GMT 2016
 * XML source: /home/chrisflatley/Tenode/Projects/baleen/baleen/baleen-uima/src/main/resources/types/language_type_system.xml
 * @generated */
public class Pattern extends Base {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Pattern.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Pattern() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Pattern(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Pattern(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Pattern(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: source

  /** getter for source - gets The source entity (first entity in the sentence)
   * @generated
   * @return value of the feature 
   */
  public Base getSource() {
    if (Pattern_Type.featOkTst && ((Pattern_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "uk.gov.dstl.baleen.types.language.Pattern");
    return (Base)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Pattern_Type)jcasType).casFeatCode_source)));}
    
  /** setter for source - sets The source entity (first entity in the sentence) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setSource(Base v) {
    if (Pattern_Type.featOkTst && ((Pattern_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "uk.gov.dstl.baleen.types.language.Pattern");
    jcasType.ll_cas.ll_setRefValue(addr, ((Pattern_Type)jcasType).casFeatCode_source, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: target

  /** getter for target - gets The target entity (last entity in the sentence)
   * @generated
   * @return value of the feature 
   */
  public Base getTarget() {
    if (Pattern_Type.featOkTst && ((Pattern_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "uk.gov.dstl.baleen.types.language.Pattern");
    return (Base)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Pattern_Type)jcasType).casFeatCode_target)));}
    
  /** setter for target - sets The target entity (last entity in the sentence) 
   * @generated
   * @param v value to set into the feature 
   */
  public void setTarget(Base v) {
    if (Pattern_Type.featOkTst && ((Pattern_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "uk.gov.dstl.baleen.types.language.Pattern");
    jcasType.ll_cas.ll_setRefValue(addr, ((Pattern_Type)jcasType).casFeatCode_target, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: words

  /** getter for words - gets The normal form for this lemma.
   * @generated
   * @return value of the feature 
   */
  public String getWords() {
    if (Pattern_Type.featOkTst && ((Pattern_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "uk.gov.dstl.baleen.types.language.Pattern");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Pattern_Type)jcasType).casFeatCode_words);}
    
  /** setter for words - sets The normal form for this lemma. 
   * @generated
   * @param v value to set into the feature 
   */
  public void setWords(String v) {
    if (Pattern_Type.featOkTst && ((Pattern_Type)jcasType).casFeat_words == null)
      jcasType.jcas.throwFeatMissing("words", "uk.gov.dstl.baleen.types.language.Pattern");
    jcasType.ll_cas.ll_setStringValue(addr, ((Pattern_Type)jcasType).casFeatCode_words, v);}    
  }

    