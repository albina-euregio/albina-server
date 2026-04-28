// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "publication_checklists", indexes = {
        @Index(name = "publication_checklists_DATE_REGION_IDX", columnList = "DATE,REGION_ID"),
})
@Serdeable
public class PublicationChecklist extends AbstractPersistentObject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REGION_ID", nullable = false)
    private Region region;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "DATE", nullable = false)
    private ZonedDateTime date;

    @Column(name = "TIMESTAMP", nullable = false)
    private ZonedDateTime timestamp;

    @OneToMany(mappedBy = "checklist", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<PublicationChecklistItem> items = new ArrayList<>();

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    public void setDate(ZonedDateTime date) {
        this.date = date;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<PublicationChecklistItem> getItems() {
        return items;
    }

    public void setItems(List<PublicationChecklistItem> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(item -> item.setChecklist(this));
            this.items.addAll(items);
        }
    }
}
