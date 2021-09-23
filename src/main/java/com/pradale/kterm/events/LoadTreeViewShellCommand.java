package com.pradale.kterm.events;

import com.pradale.kterm.domain.type.ShellCommand;
import lombok.Data;

@Data
public class LoadTreeViewShellCommand {
    private ShellCommand shellCommand;
}
