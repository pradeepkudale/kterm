package com.pradale.kterm.events;

import com.pradale.kterm.domain.type.ShellCommand;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoadShellCommandEvent {
    private ShellCommand shellCommand;
}
