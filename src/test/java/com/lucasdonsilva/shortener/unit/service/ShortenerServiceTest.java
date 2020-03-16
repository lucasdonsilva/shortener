package com.lucasdonsilva.shortener.unit.service;

import com.lucasdonsilva.shortener.document.ShortenerDocument;
import com.lucasdonsilva.shortener.dto.RequestShortenerDTO;
import com.lucasdonsilva.shortener.dto.ResponseShortenerDTO;
import com.lucasdonsilva.shortener.exception.InvalidUrlException;
import com.lucasdonsilva.shortener.exception.NotFoundException;
import com.lucasdonsilva.shortener.mapper.ShortenerMapper;
import com.lucasdonsilva.shortener.repository.ShortenerRepository;
import com.lucasdonsilva.shortener.service.ShortenerService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ShortenerServiceTest {

    @Mock
    private ShortenerRepository repository;

    @Mock
    private ShortenerMapper mapper;

    @InjectMocks
    private ShortenerService service;

    @Test
    @DisplayName("Create and return alias successfully")
    public void createSuccess() throws InvalidUrlException {

        var request = new RequestShortenerDTO();
        request.setUrl("http://www.google.com");

        var document = new ShortenerDocument();
        document.setUrl("http://www.google.com");
        document.setAlias("abc");

        when(repository.existsById(anyString())).thenReturn(false);
        when(mapper.mapToDocument(any(RequestShortenerDTO.class))).thenReturn(document);
        when(repository.save(document)).thenReturn(document);

        var response = service.create(request);

        assertEquals("abc", response);
        verify(repository).existsById(anyString());
        verify(mapper).mapToDocument(any());
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Throw error when try create alias")
    public void createError() {

        var request = new RequestShortenerDTO();
        request.setUrl("www.google.com");

        InvalidUrlException exception = assertThrows(InvalidUrlException.class, () -> service.create(request));
        assertEquals("url is invalid.", exception.getMessage());
    }

    @Test
    @DisplayName("Find and return alias successfully")
    public void findByAliasSuccess() throws NotFoundException {

        var document = new ShortenerDocument();
        document.setUrl("http://www.google.com");
        document.setAccess(1);
        var alias = "abc";

        when(repository.findByAlias(alias)).thenReturn(of(document));
        when(repository.save(document)).thenReturn(document);

        var response = service.findByAlias("abc");

        assertEquals(document.getUrl(), response);
        verify(repository).findByAlias(alias);
        verify(repository).save(any(ShortenerDocument.class));
    }

    @Test
    @DisplayName("Throw error when try find alias")
    public void findByAliasError() {

        var alias = "abc";

        when(repository.findByAlias(alias)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> service.findByAlias("abc"));

        assertEquals("alias " + alias + " not found.", exception.getMessage());
        verify(repository).findByAlias(alias);
    }

    @Test
    @DisplayName("Find top ten successfully")
    public void findTopTen() {

        var doc1 = new ShortenerDocument();
        doc1.setId(new ObjectId());
        doc1.setUrl("www.google.com");
        doc1.setAlias("abc");
        doc1.setAccess(10);

        var doc2 = new ShortenerDocument();
        doc2.setId(new ObjectId());
        doc2.setUrl("www.facebook.com");
        doc2.setAlias("efg");
        doc2.setAccess(20);

        var doc3 = new ShortenerDocument();
        doc3.setId(new ObjectId());
        doc3.setUrl("www.g1.globo.com");
        doc3.setAlias("hij");
        doc3.setAccess(50);

        var res1 = new ResponseShortenerDTO();
        res1.setUrl("www.google.com");
        res1.setAlias("abc");
        res1.setAccess(10);

        var res2 = new ResponseShortenerDTO();
        res2.setUrl("www.facebook.com");
        res2.setAlias("efg");
        res2.setAccess(20);

        var res3 = new ResponseShortenerDTO();
        res3.setUrl("www.g1.globo.com");
        res3.setAlias("hij");
        res3.setAccess(50);

        var documents = asList(doc3, doc2, doc1);
        var response = asList(res3, res2, res1);

        when(repository.findTop10ByOrderByAccessDesc()).thenReturn(documents);
        when(mapper.mapToResponses(documents)).thenReturn(response);

        var list = service.findTopTen();

        assertEquals(doc3.getUrl(), list.get(0).getUrl());
        assertEquals(doc3.getAlias(), list.get(0).getAlias());
        assertEquals(doc3.getAccess(), list.get(0).getAccess());
        assertEquals(doc2.getUrl(), list.get(1).getUrl());
        assertEquals(doc2.getAlias(), list.get(1).getAlias());
        assertEquals(doc2.getAccess(), list.get(1).getAccess());
        assertEquals(doc1.getUrl(), list.get(2).getUrl());
        assertEquals(doc1.getAlias(), list.get(2).getAlias());
        assertEquals(doc1.getAccess(), list.get(2).getAccess());
        verify(repository).findTop10ByOrderByAccessDesc();
        verify(mapper).mapToResponses(documents);
    }
}
