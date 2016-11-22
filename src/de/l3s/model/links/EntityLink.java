package de.l3s.model.links;

import de.l3s.model.Entity;

public abstract class EntityLink extends Link {
	private Entity entity;

	public EntityLink(Integer startPosition, Integer endPosition) {
		super(startPosition, endPosition);
	}

	public EntityLink() {
	}

	public Entity getEntity() {
		return this.entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public String getSomeName() {
		return this.entity.getSomeName();
	}
}
