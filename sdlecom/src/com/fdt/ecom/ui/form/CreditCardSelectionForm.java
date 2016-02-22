package com.fdt.ecom.ui.form;

import java.util.List;

import com.fdt.ecom.entity.CreditCard;
import com.fdt.sdl.styledesigner.util.PageStyleUtil;

public class CreditCardSelectionForm {

    private List<CreditCard> creditCardList;

    private Long selectedCardId;

    public CreditCardSelectionForm(List<CreditCard> creditCardList) {
        this.creditCardList = creditCardList;
    }

    public CreditCardSelectionForm() {

    }

    public List<CreditCard> getCreditCardList() {
        return creditCardList;
    }

    public void setCreditCardList(List<CreditCard> creditCardList) {
        this.creditCardList = creditCardList;
    }

    public Long getSelectedCardId() {
        return selectedCardId;
    }

    public void setSelectedCardId(Long selectedCardId) {
        this.selectedCardId = selectedCardId;
    }

    public Boolean isCardExpired(Integer index) {
        return PageStyleUtil.isCardExpired(creditCardList.get(index));
    }

    public Boolean areAllCardsExpired() {
        return PageStyleUtil.areAllCardsExpired(creditCardList);
    }

}