// This class was generated by the JAXRPC SI, do not edit.
// Contents subject to change without notice.
// JAX-RPC Standard Implementation (1.1.3, build R1)
// Generated source version: 1.1.3

package org.jboss.test.ws.jaxrpc.jbws751;


public class ShareTransaction_Type {
    protected java.util.Calendar postDate;
    protected int sequenceNumber;
    protected java.lang.String transactionCode;
    protected int postTime;
    protected java.lang.Double miscellaneousAmount;
    protected double amount;
    protected double balance;
    protected java.lang.String reqFlags;
    protected java.lang.Short itemNumber;
    protected java.lang.Integer uniqueNumber;
    protected java.lang.String comment;
    protected java.lang.String auxComment;
    protected short tellerId;
    
    public ShareTransaction_Type() {
    }
    
    public ShareTransaction_Type(java.util.Calendar postDate, int sequenceNumber, java.lang.String transactionCode, int postTime, java.lang.Double miscellaneousAmount, double amount, double balance, java.lang.String reqFlags, java.lang.Short itemNumber, java.lang.Integer uniqueNumber, java.lang.String comment, java.lang.String auxComment, short tellerId) {
        this.postDate = postDate;
        this.sequenceNumber = sequenceNumber;
        this.transactionCode = transactionCode;
        this.postTime = postTime;
        this.miscellaneousAmount = miscellaneousAmount;
        this.amount = amount;
        this.balance = balance;
        this.reqFlags = reqFlags;
        this.itemNumber = itemNumber;
        this.uniqueNumber = uniqueNumber;
        this.comment = comment;
        this.auxComment = auxComment;
        this.tellerId = tellerId;
    }
    
    public java.util.Calendar getPostDate() {
        return postDate;
    }
    
    public void setPostDate(java.util.Calendar postDate) {
        this.postDate = postDate;
    }
    
    public int getSequenceNumber() {
        return sequenceNumber;
    }
    
    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    
    public java.lang.String getTransactionCode() {
        return transactionCode;
    }
    
    public void setTransactionCode(java.lang.String transactionCode) {
        this.transactionCode = transactionCode;
    }
    
    public int getPostTime() {
        return postTime;
    }
    
    public void setPostTime(int postTime) {
        this.postTime = postTime;
    }
    
    public java.lang.Double getMiscellaneousAmount() {
        return miscellaneousAmount;
    }
    
    public void setMiscellaneousAmount(java.lang.Double miscellaneousAmount) {
        this.miscellaneousAmount = miscellaneousAmount;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public java.lang.String getReqFlags() {
        return reqFlags;
    }
    
    public void setReqFlags(java.lang.String reqFlags) {
        this.reqFlags = reqFlags;
    }
    
    public java.lang.Short getItemNumber() {
        return itemNumber;
    }
    
    public void setItemNumber(java.lang.Short itemNumber) {
        this.itemNumber = itemNumber;
    }
    
    public java.lang.Integer getUniqueNumber() {
        return uniqueNumber;
    }
    
    public void setUniqueNumber(java.lang.Integer uniqueNumber) {
        this.uniqueNumber = uniqueNumber;
    }
    
    public java.lang.String getComment() {
        return comment;
    }
    
    public void setComment(java.lang.String comment) {
        this.comment = comment;
    }
    
    public java.lang.String getAuxComment() {
        return auxComment;
    }
    
    public void setAuxComment(java.lang.String auxComment) {
        this.auxComment = auxComment;
    }
    
    public short getTellerId() {
        return tellerId;
    }
    
    public void setTellerId(short tellerId) {
        this.tellerId = tellerId;
    }
}
