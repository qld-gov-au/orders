package au.gov.qld.bdm.orders.entity;

public enum CartState {
	NEW(0), ADDED(1), PAID(2);
	
	private final int order;
	
	private CartState(int order) {
		this.order = order;
	}
	
	public boolean canUpgrade(CartState upgrade) {
		return this.order <= upgrade.order;
	}
}
