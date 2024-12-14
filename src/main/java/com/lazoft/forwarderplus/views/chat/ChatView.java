package com.lazoft.forwarderplus.views.chat;

import com.lazoft.forwarderplus.components.avataritem.AvatarItem;
import com.lazoft.forwarderplus.views.MainLayout;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@PageTitle("Chat")
@Route(value = "chat", layout = MainLayout.class)
@PermitAll
public class ChatView extends Composite<VerticalLayout> {

    public ChatView() {
        HorizontalLayout layoutRow = new HorizontalLayout();
        VerticalLayout layoutColumn2 = new VerticalLayout();
        MultiSelectListBox avatarItems = new MultiSelectListBox();
        VerticalLayout layoutColumn3 = new VerticalLayout();
        MessageList messageList = new MessageList();
        MessageInput messageInput = new MessageInput();
        getContent().setWidth("100%");
        getContent().getStyle().set("flex-grow", "1");
        layoutRow.addClassName(Gap.MEDIUM);
        layoutRow.setWidth("100%");
        layoutRow.getStyle().set("flex-grow", "1");
        layoutColumn2.setWidth("300px");
        layoutColumn2.getStyle().set("flex-grow", "1");
        avatarItems.setWidth("min-content");
        setAvatarItemsSampleData(avatarItems);
        layoutColumn3.setWidth("100%");
        layoutColumn3.getStyle().set("flex-grow", "1");
        messageList.setWidth("100%");
        messageList.getStyle().set("flex-grow", "1");
        setMessageListSampleData(messageList);
        messageInput.setWidth("100%");
        messageInput.setHeight("56px");
        getContent().add(layoutRow);
        layoutRow.add(layoutColumn2);
        layoutColumn2.add(avatarItems);
        layoutRow.add(layoutColumn3);
        layoutColumn3.add(messageList);
        layoutColumn3.add(messageInput);
    }

    private void setAvatarItemsSampleData(MultiSelectListBox multiSelectListBox) {
        record Person(String name, String profession) {
        }
        ;
        List<Person> data = List.of(new Person("Aria Bailey", "Endocrinologist"), new Person("Aaliyah Butler", "Nephrologist"), new Person("Eleanor Price", "Ophthalmologist"), new Person("Allison Torres", "Allergist"), new Person("Madeline Lewis", "Gastroenterologist"));
        multiSelectListBox.setItems(data);
        multiSelectListBox.setRenderer(new ComponentRenderer(item -> {
            AvatarItem avatarItem = new AvatarItem();
            avatarItem.setHeading(((Person) item).name);
            avatarItem.setDescription(((Person) item).profession);
            avatarItem.setAvatar(new Avatar(((Person) item).name));
            return avatarItem;
        }));
    }

    private void setMessageListSampleData(MessageList messageList) {
        MessageListItem message1 = new MessageListItem("Nature does not hurry, yet everything gets accomplished.",
                LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC), "Matt Mambo");
        message1.setUserColorIndex(1);
        MessageListItem message2 = new MessageListItem(
                "Using your talent, hobby or profession in a way that makes you contribute with something good to this world is truly the way to go.",
                LocalDateTime.now().minusMinutes(55).toInstant(ZoneOffset.UTC), "Linsey Listy");
        message2.setUserColorIndex(2);
        messageList.setItems(message1, message2);
    }
}
