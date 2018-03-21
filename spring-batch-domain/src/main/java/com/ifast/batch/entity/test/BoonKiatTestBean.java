package com.ifast.batch.entity.test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "BOON_KIAT_TEST")
public class BoonKiatTestBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8522120350856828284L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MOBILE_PUSH_NOTIFICATION_SEQ")
	@SequenceGenerator(name = "MOBILE_PUSH_NOTIFICATION_SEQ", sequenceName = "MOBILE_PUSH_NOTIFICATION_SEQ", allocationSize = 1)
	@Column(name = "ID")
	private Long id;
	
	@Column(name = "BID_PRICE")
	private BigDecimal bidPrice;
	
	@Column(name = "ASK_PRICE")
	private BigDecimal askPrice;
	
	@Column(name = "CREATE_DATE")
	private Date createDate;
	
	@Column(name = "APPROVE_DATE")
	private Date approveDate;
	
	@Column(name = "APPROVE_BY")
	private String approveBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BigDecimal getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(BigDecimal bidPrice) {
		this.bidPrice = bidPrice;
	}

	public BigDecimal getAskPrice() {
		return askPrice;
	}

	public void setAskPrice(BigDecimal askPrice) {
		this.askPrice = askPrice;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getApproveDate() {
		return approveDate;
	}

	public void setApproveDate(Date approveDate) {
		this.approveDate = approveDate;
	}

	public String getApproveBy() {
		return approveBy;
	}

	public void setApproveBy(String approveBy) {
		this.approveBy = approveBy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BoonKiatTestBean other = (BoonKiatTestBean) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
